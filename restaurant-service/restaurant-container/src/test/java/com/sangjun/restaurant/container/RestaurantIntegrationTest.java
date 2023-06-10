package com.sangjun.restaurant.container;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.Product;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.sangjun.kafka.order.avro.model.RestaurantOrderStatus;
import com.sangjun.restaurant.dataaccess.entity.OrderApprovalEntity;
import com.sangjun.restaurant.dataaccess.repository.OrderApprovalJpaRepository;
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
@SpringBootTest(classes = TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RestaurantIntegrationTest {
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID_2 = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID_2 = UUID.randomUUID();

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${restaurant-service.restaurant-approval-request-topic-name}")
    private String restaurantApprovalRequestTopic;

    @Value("${restaurant-service.restaurant-approval-response-topic-name}")
    private String restaurantApprovalResponseTopic;

    @Autowired
    private RestaurantJpaRepository restaurantJpaRepository;

    @Autowired
    private OrderApprovalJpaRepository orderApprovalJpaRepository;

    @Autowired
    private KafkaTemplate<String, RestaurantApprovalRequestAvroModel> restaurantRequestKt;

    @Autowired
    private ConsumerFactory<String, RestaurantApprovalResponseAvroModel> restaurantResponseCf;

    private Consumer<String, RestaurantApprovalResponseAvroModel> restaurantResponseConsumer;

    @BeforeAll
    void setUp() {
        restaurantResponseConsumer =
                restaurantResponseCf.createConsumer("restaurant-response", "1");
        restaurantResponseConsumer.subscribe(List.of(restaurantApprovalResponseTopic));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            // ignored
        }
    }

    @AfterAll
    void tearDown() {
        restaurantResponseConsumer.close();
    }

    @AfterEach
    void clearKafkaMessages() {
        consumeRestaurantResponseTopic();
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
                        "TABLE_SCHEMA in ('restaurant')");
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
    void 주문승인_메시지를_수신했을때_정상적인_주문이면_주문을_승인한다() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .restaurantActive(true)
                .restaurantName("myRestaurant")
                .productName("test-prod-1")
                .productPrice(price)
                .productId(PRODUCT_ID)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(price)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.APPROVED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.APPROVED);
    }


    @Test
    void 주문승인_메시지를_수신했을때_식당이_해당_제품을_취급하지않으면_주문을_거부한다() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");
        BigDecimal price2 = new BigDecimal("3300");
        BigDecimal sum = price2.add(price);

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .productName("test-prod-1")
                .productPrice(price)
                .build());

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID_2)
                .restaurantName("myRestaurant2")
                .restaurantActive(true)
                .productId(PRODUCT_ID_2)
                .productAvailable(true)
                .productName("test-prod-2")
                .productPrice(price2)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();
        Product product2 = Product.newBuilder()
                .setId(PRODUCT_ID_2.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(sum)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product, product2))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.REJECTED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.REJECTED);
    }

    @Test
    void 주문승인_메시지를_수신했을때_정상적인_주문이면_복수의_물품에_대해_주문이_승인된다() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");
        BigDecimal price2 = new BigDecimal("3300");
        BigDecimal sum = price2.add(price);

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .productName("test-prod-1")
                .productPrice(price)
                .build());

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID_2)
                .productAvailable(true)
                .productName("test-prod-2")
                .productPrice(price2)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();
        Product product2 = Product.newBuilder()
                .setId(PRODUCT_ID_2.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(sum)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product, product2))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.APPROVED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.APPROVED);
    }

    @Test
    void 주문승인_메시지를_수신했을때_가격_합계가_다르면_주문이_거부된다_물품2개인_경우() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");
        BigDecimal price2 = new BigDecimal("3300");
        BigDecimal falseSum = new BigDecimal("10000");

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .productName("test-prod-1")
                .productPrice(price)
                .build());

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID_2)
                .productAvailable(true)
                .productName("test-prod-2")
                .productPrice(price2)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();
        Product product2 = Product.newBuilder()
                .setId(PRODUCT_ID_2.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(falseSum)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product, product2))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.REJECTED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.REJECTED);
    }

    @Test
    void 주문승인_메시지를_수신했을때_가격_합계가_다르면_주문이_거부된다_복수의_단일상품인_경우() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");
        BigDecimal falseSum = new BigDecimal("2200"); // must be 4400 because quantity is 2 (see below)

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantName("myRestaurant")
                .restaurantActive(true)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .productName("test-prod-1")
                .productPrice(price)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(2)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(falseSum)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.REJECTED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.REJECTED);
    }

    @Test
    void 주문승인_메시지를_수신했을때_상품이_주문불가상태_이면_주문을_거부한다() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .productId(PRODUCT_ID)
                .productAvailable(false)
                .restaurantActive(true)
                .restaurantName("myRestaurant")
                .productName("test-prod-1")
                .productPrice(price)
                .productId(PRODUCT_ID)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(price)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.REJECTED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.REJECTED);
    }

    @Test
    void 주문승인_메시지를_수신했을때_식당이_이용불가상태_이면_주문을_거부한다() throws ExecutionException, InterruptedException {
        //given
        BigDecimal price = new BigDecimal("2200");

        saveRestaurantEntity(RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .productId(PRODUCT_ID)
                .productAvailable(true)
                .restaurantActive(false)
                .restaurantName("myRestaurant")
                .productName("test-prod-1")
                .productPrice(price)
                .productId(PRODUCT_ID)
                .build());

        Product product = Product.newBuilder()
                .setId(PRODUCT_ID.toString())
                .setQuantity(1)
                .build();

        RestaurantApprovalRequestAvroModel msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setOrderId(ORDER_ID.toString())
                .setPrice(price)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product))
                .setSagaId(UUID.randomUUID().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .build();

        //when
        restaurantRequestKt.send(restaurantApprovalRequestTopic, ORDER_ID.toString(), msg)
                .get();

        //then
        Thread.sleep(200);

        OrderApprovalEntity orderApprovalEntity = orderApprovalJpaRepository.findByOrderId(ORDER_ID).get();
        assertThat(orderApprovalEntity.getRestaurantId())
                .isEqualTo(RESTAURANT_ID);
        assertThat(orderApprovalEntity.getStatus())
                .isEqualTo(OrderApprovalStatus.REJECTED);

        checkRestaurantResponse(RESTAURANT_ID,
                ORDER_ID,
                com.sangjun.kafka.order.avro.model.OrderApprovalStatus.REJECTED);
    }

    private void checkRestaurantResponse(UUID restaurantId,
                                         UUID orderId,
                                         com.sangjun.kafka.order.avro.model.OrderApprovalStatus orderApprovalStatus) {
        Map<String, RestaurantApprovalResponseAvroModel> resultMap = consumeRestaurantResponseTopic();

        assertThat(resultMap.size())
                .isEqualTo(1);

        RestaurantApprovalResponseAvroModel response = resultMap.get(orderId.toString());
        assertThat(response.getRestaurantId())
                .isEqualTo(restaurantId.toString());
        assertThat(response.getOrderId())
                .isEqualTo(orderId.toString());
        assertThat(response.getOrderApprovalStatus())
                .isEqualTo(orderApprovalStatus);
    }

    private Map<String, RestaurantApprovalResponseAvroModel> consumeRestaurantResponseTopic() {
        ConsumerRecords<String, RestaurantApprovalResponseAvroModel> records =
                KafkaTestUtils.getRecords(restaurantResponseConsumer, 100);
        Map<String, RestaurantApprovalResponseAvroModel> resultMap = new HashMap<>();
        records.forEach(rec -> resultMap.put(rec.key(), rec.value()));
        restaurantResponseConsumer.commitSync();
        return resultMap;
    }

    private void saveRestaurantEntity(RestaurantEntity restaurantEntity) {
        restaurantJpaRepository.save(restaurantEntity);
        entityManager.flush();
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }


}
