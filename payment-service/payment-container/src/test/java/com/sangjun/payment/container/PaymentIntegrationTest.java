package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.kafka.order.avro.model.PaymentOrderStatus;
import com.sangjun.kafka.order.avro.model.PaymentRequestAvroModel;
import com.sangjun.kafka.order.avro.model.PaymentResponseAvroModel;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.book.BookShelveId;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.service.ports.output.repository.*;
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
@SpringBootTest(classes = IntegrationTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PaymentIntegrationTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();

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

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookShelveRepository bookShelveRepository;

    @Autowired
    private TestHelper testHelper;

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

    @AfterEach
    void cleanKafkaEvents() {
        발행된_이벤트메세지_모두_가져오기();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void 결제_완료() throws ExecutionException, InterruptedException {
        //given
        Book firmBook = 회사_장부_생성();
        Book customerBook = 고객_장부_생성();
        Book restaurantBook = 식당_장부_생성();
        Money customerInitialBalance = Money.of(new BigDecimal("100000"));
        고객에게_충전금_부여(firmBook, customerBook, customerInitialBalance);

        사전조건_반영();
        //when
        PaymentRequestAvroModel msg = 결제요청_메세지_생성(new BigDecimal("3000"), PaymentOrderStatus.PENDING);
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();
        //then
        Thread.sleep(200);
        Payment payment = 결제정보_확인(Money.of(msg.getPrice()), PaymentStatus.COMPLETED);

        고객장부_업데이트_확인(customerBook, customerInitialBalance.subtract(payment.getPrice()));
        식당장부_업데이트_확인(restaurantBook, payment.getPrice());
        결제완료_이벤트_발행_확인();
    }

    private Book 식당_장부_생성() {
        Book restaurantBook = testHelper.saveBook(RESTAURANT_ID.toString(), BookOwnerType.RESTAURANT, EntryIdType.UUID);
        return restaurantBook;
    }

    private Book 고객_장부_생성() {
        Book customerBook = testHelper.saveBook(CUSTOMER_ID.toString(), BookOwnerType.CUSTOMER, EntryIdType.UUID);
        return customerBook;
    }

    private Book 회사_장부_생성() {
        Book firmBook = testHelper.saveBook(UUID.randomUUID().toString(), BookOwnerType.FIRM, EntryIdType.UUID);
        return firmBook;
    }

    private void 고객에게_충전금_부여(Book firmBook, Book customerBook, Money initialBalance) {
        firmBook.transact(customerBook, initialBalance, "", "");
    }

    private void 사전조건_반영() {
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    private void 결제완료_이벤트_발행_확인() {
        Map<String, PaymentResponseAvroModel> eventList = 발행된_이벤트메세지_모두_가져오기();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel responseMsg = eventList.get(ORDER_ID.toString());
        발행된_이벤트메세지_확인(responseMsg,
                com.sangjun.kafka.order.avro.model.PaymentStatus.COMPLETED,
                Money.of("3000").getAmount());
    }

    private static void 발행된_이벤트메세지_확인(PaymentResponseAvroModel responseMsg,
                                      com.sangjun.kafka.order.avro.model.PaymentStatus paymentStatus,
                                      BigDecimal price) {
        assertThat(responseMsg.getOrderId())
                .isEqualTo(ORDER_ID.toString());
        assertThat(responseMsg.getCustomerId())
                .isEqualTo(CUSTOMER_ID.toString());
        assertThat(responseMsg.getRestaurantId())
                .isEqualTo(RESTAURANT_ID.toString());
        assertThat(responseMsg.getPaymentStatus())
                .isEqualTo(paymentStatus);
        assertThat(responseMsg.getPrice())
                .isEqualTo(price);
    }

    private PaymentRequestAvroModel 결제요청_메세지_생성(BigDecimal price, PaymentOrderStatus paymentOrderStatus) {
        return PaymentRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setPrice(price)
                .setSagaId("")
                .setRestaurantId(RESTAURANT_ID.toString())
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentOrderStatus(paymentOrderStatus)
                .build();
    }

    private Payment 결제정보_확인(Money expectedPrice, PaymentStatus paymentStatus) {
        Payment payment = paymentRepository
                .findByOrderId(new OrderId(ORDER_ID))
                .get();

        assertThat(payment.getPrice())
                .isEqualTo(expectedPrice);
        assertThat(payment.getPaymentStatus())
                .isEqualTo(paymentStatus);
        assertThat(payment.getRestaurantId().getValue())
                .isEqualTo(RESTAURANT_ID);
        assertThat(payment.getCustomerId().getValue())
                .isEqualTo(CUSTOMER_ID);
        return payment;
    }

    private void 식당장부_업데이트_확인(Book restaurantBook, Money expectedPrice) {
        장부_업데이트_확인(restaurantBook, BookOwnerType.RESTAURANT, RESTAURANT_ID.toString(), expectedPrice);
    }

    private void 고객장부_업데이트_확인(Book customerBook, Money expectedPrice) {
        장부_업데이트_확인(customerBook, BookOwnerType.CUSTOMER, CUSTOMER_ID.toString(), expectedPrice);
    }

    private void 장부_업데이트_확인(Book restaurantBook, BookOwnerType bookOwnerType, String bookOwnerId, Money expectedPrice) {
        UUID restaurantShelveId = bookShelveRepository.findIdByOwnerType(bookOwnerType);
        Book foundRestaurantBook = bookRepository
                .findByBookShelveIdAndBookOwner_uuid(new BookShelveId(restaurantShelveId), UUID.fromString(bookOwnerId))
                .get();

        assertThat(foundRestaurantBook.getTotalBalance().getCurrentBalance())
                .isEqualTo(expectedPrice);
        assertThat(foundRestaurantBook.getBookEntryList().getSize())
                .isEqualTo(restaurantBook.getBookEntryList().getSize() + 1);
    }

    @Test
    void 결제_취소() throws ExecutionException, InterruptedException {
        //given
        고객_장부_생성();
        식당_장부_생성();
        Money price = Money.of("3000");
        결제완료정보_생성(price);

        사전조건_반영();

        //when
        PaymentRequestAvroModel msg = 결제요청_메세지_생성(price.getAmount(), PaymentOrderStatus.CANCELLED);
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);
        결제상태가_취소로_변경됨을_확인();
        결제취소_이벤트_발행_확인(price);
    }

    private void 결제완료정보_생성(Money price) {
        Payment payment = Payment.builder()
                .orderId(new OrderId(ORDER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .price(price)
                .build();
        payment.initialize();
        payment.complete();
        paymentRepository.save(payment);
    }

    private void 결제상태가_취소로_변경됨을_확인() {
        Payment foundPayment = paymentRepository.findByOrderId(new OrderId(ORDER_ID)).get();
        assertThat(foundPayment.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCELLED);
    }

    private void 결제취소_이벤트_발행_확인(Money price) {
        Map<String, PaymentResponseAvroModel> eventList = 발행된_이벤트메세지_모두_가져오기();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel paymentResponseAvroModel = eventList.get(ORDER_ID.toString());

        발행된_이벤트메세지_확인(paymentResponseAvroModel,
                com.sangjun.kafka.order.avro.model.PaymentStatus.CANCELLED,
                price.getAmount());
    }

    @Test
    void 결제_실패() throws ExecutionException, InterruptedException {
        //given
        Money price = Money.of("4000");
        고객_장부_생성();
        식당_장부_생성();

        사전조건_반영();

        //when
        PaymentRequestAvroModel msg = 결제요청_메세지_생성(price.getAmount(), PaymentOrderStatus.PENDING);
        paymentRequestKt.send(paymentRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);
        결제정보_확인(Money.of(msg.getPrice()), PaymentStatus.FAILED);
        결제실패_이벤트_발행_확인(price);
    }

    private void 결제실패_이벤트_발행_확인(Money price) {
        Map<String, PaymentResponseAvroModel> eventList = 발행된_이벤트메세지_모두_가져오기();
        assertThat(eventList.size()).isEqualTo(1);

        PaymentResponseAvroModel paymentResponseAvroModel = eventList.get(ORDER_ID.toString());

        발행된_이벤트메세지_확인(paymentResponseAvroModel,
                com.sangjun.kafka.order.avro.model.PaymentStatus.FAILED,
                price.getAmount());
    }

    private Map<String, PaymentResponseAvroModel> 발행된_이벤트메세지_모두_가져오기() {
        ConsumerRecords<String, PaymentResponseAvroModel> result =
                KafkaTestUtils.getRecords(paymentResponseAvroModelConsumer, 100);
        Map<String, PaymentResponseAvroModel> records = new HashMap<>();
        result.forEach(res -> records.put(res.key(), res.value()));
        paymentResponseAvroModelConsumer.commitSync();

        return records;
    }
}
