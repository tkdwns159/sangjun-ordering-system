package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.PaymentStatus;
import com.sangjun.kafka.order.avro.model.PaymentOrderStatus;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.Payment;
import com.sangjun.payment.domain.valueobject.CreditEntryId;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.PaymentId;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;
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

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PAYMENT_ID = UUID.randomUUID();


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
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
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
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Payment payment = paymentRepository.findByOrderId(ORDER_ID).get();

        assertThat(payment.getPrice())
                .isEqualTo(Money.of(new BigDecimal("3000")));
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getCustomerId().getValue())
                .isEqualTo(CUSTOMER_ID);
    }

    @Test
    void 결제가_완료되면_결제완료_이벤트가_발행된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
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
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Map<String, PaymentResponseAvroModel> eventList = consumePaymentResponseTopic();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel paymentResponseAvroModel = eventList.get(ORDER_ID.toString());

        assertThat(paymentResponseAvroModel.getOrderId())
                .isEqualTo(ORDER_ID.toString());
        assertThat(paymentResponseAvroModel.getCustomerId())
                .isEqualTo(CUSTOMER_ID.toString());
        assertThat(paymentResponseAvroModel.getPaymentStatus())
                .isEqualTo(com.sangjun.kafka.order.avro.model.PaymentStatus.COMPLETED);
        assertThat(paymentResponseAvroModel.getPrice())
                .isEqualTo(Money.of(new BigDecimal("3000")).getAmount());
    }

    @Test
    void 결제취소시_결제취소_이벤트가_발행된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("3000")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build();

        UUID paymentId = UUID.randomUUID();

        Payment payment = Payment.builder(new OrderId(ORDER_ID), new CustomerId(CUSTOMER_ID), Money.of(new BigDecimal(3000)))
                .id(new PaymentId(paymentId))
                .createdAt(ZonedDateTime.now(ZoneId.of(ZONE_ID)))
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        creditHistoryRepository.save(creditHistory);
        creditEntryRepository.save(creditEntry);
        paymentRepository.save(payment);
        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        //when
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Map<String, PaymentResponseAvroModel> eventList = consumePaymentResponseTopic();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel paymentResponseAvroModel = eventList.get(ORDER_ID.toString());

        assertThat(paymentResponseAvroModel.getPaymentId())
                .isEqualTo(paymentId.toString());
        assertThat(paymentResponseAvroModel.getOrderId())
                .isEqualTo(ORDER_ID.toString());
        assertThat(paymentResponseAvroModel.getCustomerId())
                .isEqualTo(CUSTOMER_ID.toString());
        assertThat(paymentResponseAvroModel.getPaymentStatus())
                .isEqualTo(com.sangjun.kafka.order.avro.model.PaymentStatus.CANCELLED);
        assertThat(paymentResponseAvroModel.getPrice())
                .isEqualTo(Money.of(new BigDecimal("3000")).getAmount());
    }

    @Test
    void 결제가_취소시_결제데이터는_결제취소상태로_변경된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("3000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
                        Money.of(new BigDecimal("3000")),
                        TransactionType.DEBIT)
                .id(new CreditHistoryId(UUID.randomUUID()))
                .build();


        Payment payment = Payment.builder(new OrderId(ORDER_ID), new CustomerId(CUSTOMER_ID), Money.of(new BigDecimal(3000)))
                .id(new PaymentId(PAYMENT_ID))
                .createdAt(ZonedDateTime.now(ZoneId.of(ZONE_ID)))
                .paymentStatus(PaymentStatus.COMPLETED)
                .build();

        creditHistoryRepository.save(creditHistory);
        creditEntryRepository.save(creditEntry);
        paymentRepository.save(payment);
        entityManager.flush();

        TestTransaction.flagForCommit();
        TestTransaction.end();

        //when
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);
        Payment foundPayment = paymentRepository.findByOrderId(ORDER_ID).get();

        assertThat(foundPayment.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void 결제실패시_결제데이터가_실패상태로_저장된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("4000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
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
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(100);

        Payment foundPayment = paymentRepository.findByOrderId(ORDER_ID).get();

        assertThat(foundPayment.getOrderId().getValue())
                .isEqualTo(ORDER_ID);
        assertThat(foundPayment.getCustomerId().getValue())
                .isEqualTo(CUSTOMER_ID);
        assertThat(foundPayment.getPrice())
                .isEqualTo(Money.of(new BigDecimal("4000")));
        assertThat(foundPayment.getPaymentStatus())
                .isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void 결제실패시_결제실패이벤트가_발행된다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        PaymentRequestAvroModel msg = PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(new BigDecimal("4000"))
                .setSagaId("")
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .build();

        CreditEntry creditEntry = CreditEntry.builder(new CustomerId(CUSTOMER_ID))
                .id(new CreditEntryId(UUID.randomUUID()))
                .totalCreditAmount(Money.of(new BigDecimal("3000")))
                .build();

        CreditHistory creditHistory = CreditHistory.builder(
                        new CustomerId(CUSTOMER_ID),
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
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(100);
        Map<String, PaymentResponseAvroModel> result = consumePaymentResponseTopic();
        PaymentResponseAvroModel response = result.get(ORDER_ID.toString());

        assertThat(response.getPaymentId())
                .isNotNull();
        assertThat(response.getOrderId())
                .isEqualTo(ORDER_ID.toString());
        assertThat(response.getCustomerId())
                .isEqualTo(CUSTOMER_ID.toString());
        assertThat(response.getFailureMessages().isEmpty())
                .isNotNull();
        assertThat(response.getPrice())
                .isEqualTo(Money.of(new BigDecimal("4000")).getAmount());
        assertThat(response.getPaymentStatus())
                .isEqualTo(com.sangjun.kafka.order.avro.model.PaymentStatus.FAILED);
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
