package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.kafka.order.avro.model.PaymentOrderStatus;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.valueobject.CreditEntryId;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.TransactionType;
import com.sangjun.payment.service.ports.output.repository.CreditEntryRepository;
import com.sangjun.payment.service.ports.output.repository.CreditHistoryRepository;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@EmbeddedKafka(
        count = 3,
        partitions = 3,
        bootstrapServersProperty = "kafka-config.bootstrap-servers",
        zookeeperPort = 2182,
        zkSessionTimeout = 1000,
        zkConnectionTimeout = 1000
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.Random.class)
@Transactional
@SpringBootTest(classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentIntegrationTest {

    @Autowired
    private KafkaTemplate<String, PaymentRequestAvroModel> paymentRequestKt;

    @Autowired
    private ConsumerFactory<String, PaymentResponseAvroModel> paymentResponseAvroModelConsumerFactory;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CreditHistoryRepository creditHistoryRepository;

    @Autowired
    private CreditEntryRepository creditEntryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${payment-service.payment-request-topic-name}")
    private String paymentRequestTopic;

    @Value("${payment-service.payment-response-topic-name}")
    private String paymentResponseTopic;

    private Consumer<String, PaymentResponseAvroModel> paymentResponseAvroModelConsumer;

    @BeforeAll
    void setUp() throws InterruptedException {
        paymentResponseAvroModelConsumer =
                paymentResponseAvroModelConsumerFactory.createConsumer("test-payment-response", "1");
        paymentResponseAvroModelConsumer.subscribe(List.of(paymentResponseTopic));

        Thread.sleep(1000);
    }

    @AfterAll
    void tearDown() {
        paymentResponseAvroModelConsumer.close();
    }

    @BeforeEach
    void cleanUp() {
        // transaction 자동시작
        TestTransaction.flagForCommit();
        truncateAllTables();
        TestTransaction.end();

        // 다음 수행할 함수를 위해 transaction 재시작
        TestTransaction.start();
    }

    @AfterEach
    void cleanKafkaEvents() {
        consumePaymentResponseTopic();
    }

    private void truncateAllTables() {
        Query query = entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE");
        query.executeUpdate();

        query = entityManager.createNativeQuery(
                "SELECT 'TRUNCATE TABLE ' || TABLE_SCHEMA || '.' || TABLE_NAME || ';' FROM INFORMATION_SCHEMA.TABLES WHERE " +
                        "TABLE_SCHEMA in ('restaurant', 'payment')");
        List<String> statements = query.getResultList();

        for (String statement : statements) {
            query = entityManager.createNativeQuery(statement);
            query.executeUpdate();
        }

        query = entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE");
        query.executeUpdate();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void 결제가_완료되면_결제데이터가_저장된다() throws ExecutionException, InterruptedException {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderId.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(customerId.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("3000")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build();

        creditHistoryRepository.save(creditHistory);
        creditEntryRepository.save(creditEntry);
        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        //when
        paymentRequestKt.send(paymentRequestTopic, orderId.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Payment payment = paymentRepository.findByOrderId(orderId).get();

        assertThat(payment.getPrice())
                .isEqualTo(Money.of(new BigDecimal("3000")));
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCustomerId().getValue())
                .isEqualTo(customerId);
    }

    @Test
    void 결제가_완료되면_결제완료_이벤트가_발행된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderId.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(customerId.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(customerId))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(customerId),
                        Money.of(new BigDecimal("3000")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build();

        creditHistoryRepository.save(creditHistory);
        creditEntryRepository.save(creditEntry);
        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        //when
        paymentRequestKt.send(paymentRequestTopic, orderId.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Map<String, PaymentResponseAvroModel> eventList = consumePaymentResponseTopic();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel paymentResponseAvroModel = eventList.get(orderId.toString());

        assertThat(paymentResponseAvroModel.getOrderId())
                .isEqualTo(orderId.toString());
        assertThat(paymentResponseAvroModel.getCustomerId())
                .isEqualTo(customerId.toString());
        assertThat(paymentResponseAvroModel.getPaymentStatus())
                .isEqualTo(com.sangjun.kafka.order.avro.model.PaymentStatus.COMPLETED);
    }

    private Map<String, PaymentResponseAvroModel> consumePaymentResponseTopic() {
        ConsumerRecords<String, PaymentResponseAvroModel> result =
                KafkaTestUtils.getRecords(paymentResponseAvroModelConsumer, 100);
        Map<String, PaymentResponseAvroModel> records = new HashMap<>();
        result.forEach(res -> records.put(res.key(), res.value()));
        paymentResponseAvroModelConsumer.commitSync();

        return records;
    }
}
