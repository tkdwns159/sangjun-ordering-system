package com.sangjun.restaurant.container;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.kafka.order.avro.model.*;
import com.sangjun.restaurant.application.ports.input.service.RestaurantApprovalApplicationService;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final UUID RESTAURANT_ID = UUID.randomUUID();
    private final UUID ORDER_ID = UUID.randomUUID();

    @PersistenceContext
    private EntityManager em;

    @Value("${restaurant-service.restaurant-approval-request-topic-name}")
    private String restaurantApprovalRequestTopic;

    @Value("${restaurant-service.restaurant-approval-response-topic-name}")
    private String restaurantApprovalResponseTopic;

    @Autowired
    private KafkaTemplate<String, RestaurantApprovalRequestAvroModel> restaurantRequestKt;

    @Autowired
    private ConsumerFactory<String, RestaurantApprovalResponseAvroModel> restaurantResponseCf;
    @Autowired
    private RestaurantApprovalApplicationService restaurantApprovalService;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PendingOrderRepository pendingOrderRepository;

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
        Query query = em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE");
        query.executeUpdate();

        query = em.createNativeQuery(
                "SELECT 'TRUNCATE TABLE ' || TABLE_SCHEMA || '.' || TABLE_NAME || ';' FROM INFORMATION_SCHEMA.TABLES WHERE " +
                        "TABLE_SCHEMA in ('restaurant')");
        List<String> statements = query.getResultList();

        for (String statement : statements) {
            query = em.createNativeQuery(statement);
            query.executeUpdate();
        }

        query = em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE");
        query.executeUpdate();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void 주문승인대기_처리() throws InterruptedException {
        //given
        Restaurant restaurant = 주문받을_식당과_제품들이_등록되어있음();
        TestTransaction.flagForCommit();
        TestTransaction.end();

        var products = restaurant.getProducts();
        Product product1 = Product.newBuilder()
                .setId(products.get(0).getId().getValue().toString())
                .setQuantity(2)
                .build();
        Product product2 = Product.newBuilder()
                .setId(products.get(1).getId().getValue().toString())
                .setQuantity(3)
                .build();
        Money price = products.get(0).getPrice().multiply(2)
                .add(products.get(1).getPrice().multiply(3));

        //when
        var msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setRestaurantId(restaurant.getId().getValue().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .setCreatedAt(Instant.now())
                .setProducts(List.of(product1, product2))
                .setPrice(price.getAmount())
                .setSagaId("")
                .build();
        var key = ORDER_ID.toString();
        restaurantRequestKt.send(restaurantApprovalRequestTopic, key, msg);
        Thread.sleep(200);

        //then
        주문대기처리됨(new OrderId(ORDER_ID));
    }

    private Restaurant 주문받을_식당과_제품들이_등록되어있음() {
        var product1 = com.sangjun.restaurant.domain.entity.Product.builder()
                .name("product1")
                .price(Money.of("3300"))
                .available(true)
                .quantity(100)
                .build();
        var product2 = com.sangjun.restaurant.domain.entity.Product.builder()
                .name("product2")
                .price(Money.of("1230"))
                .available(true)
                .quantity(100)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .isActive(true)
                .products(List.of(product1, product2))
                .build();
        return restaurantRepository.save(restaurant);
    }

    private void 주문대기처리됨(OrderId orderId) {
        var pendingOrder = pendingOrderRepository.findByOrderId(orderId).get();
        assertThat(pendingOrder.getStatus())
                .isEqualTo(PendingOrderStatus.PENDING);
    }

    @Test
    void 주문승인() {
        //given
        Restaurant restaurant = 주문받을_식당과_제품들이_등록되어있음();
        PendingOrderId pendingOrderId = 승인대기중인_주문이_있음(restaurant);

        //when
        restaurantApprovalService.approveOrder(pendingOrderId.toString());

        //then
        PendingOrder pendingOrder = pendingOrderRepository.findById(pendingOrderId).get();
        assertThat(pendingOrder.getStatus())
                .isEqualTo(PendingOrderStatus.APPROVED);

        주문승인완료_메세지_전송(restaurant);
    }

    private PendingOrderId 승인대기중인_주문이_있음(Restaurant restaurant) {
        PendingOrder pendingOrder = PendingOrder.builder()
                .status(PendingOrderStatus.PENDING)
                .restaurantId(restaurant.getId())
                .orderId(new OrderId(ORDER_ID))
                .build();
        PendingOrder savedPendingOrder = pendingOrderRepository.save(pendingOrder);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        return savedPendingOrder.getId();
    }

    private void 주문승인완료_메세지_전송(Restaurant restaurant) {
        Map<String, RestaurantApprovalResponseAvroModel> responseMap = consumeRestaurantResponseTopic();
        var response = responseMap.get(ORDER_ID.toString());
        assertThat(response.getOrderId())
                .isEqualTo(ORDER_ID.toString());
        assertThat(response.getRestaurantId())
                .isEqualTo(restaurant.getId().toString());
        assertThat(response.getOrderApprovalStatus())
                .isEqualTo(OrderApprovalStatus.APPROVED);
    }

    @Test
    void 주문이_아직_승인되지_않았을때_주문대기취소() {
        //given
        Restaurant restaurant = 주문받을_식당과_제품들이_등록되어있음();
        승인대기중인_주문이_있음(restaurant);

        //when
        var msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.CANCELLED)
                .setCreatedAt(Instant.now())
                .setSagaId("")
                .build();
        var key = ORDER_ID.toString();
        restaurantRequestKt.send(restaurantApprovalRequestTopic, key, msg);

        //then
        주문대기취소됨();
    }

    private void 주문대기취소됨() {

    }

    @Test
    void 주문이_승인되었을때_주문대기취소() {
        //given
        Restaurant restaurant = 주문받을_식당과_제품들이_등록되어있음();
        승인대기중인_주문이_있음(restaurant);
        승인대기중인_주문이_승인됨();

        //when
        var msg = RestaurantApprovalRequestAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.CANCELLED)
                .setCreatedAt(Instant.now())
                .setSagaId("")
                .build();
        var key = ORDER_ID.toString();
        restaurantRequestKt.send(restaurantApprovalRequestTopic, key, msg);

        //then
        주문대기취소요청이_무시됨();
    }

    private void 승인대기중인_주문이_승인됨() {
    }

    private void 주문대기취소요청이_무시됨() {

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

}
