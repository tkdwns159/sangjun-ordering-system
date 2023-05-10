package com.sangjun.order.container;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.kafka.order.avro.model.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.PaymentStatus;
import com.sangjun.kafka.order.avro.model.RestaurantOrderStatus;
import com.sangjun.kafka.order.avro.model.*;
import com.sangjun.order.dataaccess.customer.entity.CustomerEntity;
import com.sangjun.order.dataaccess.customer.repository.CustomerJpaRepository;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddress;
import com.sangjun.order.domain.service.dto.create.OrderItem;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderIntegrationTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        count = 3,
        partitions = 3,
        bootstrapServersProperty = "kafka-config.bootstrap-servers",
        zookeeperPort = 2182,
        zkSessionTimeout = 1000,
        zkConnectionTimeout = 1000
)
@TestMethodOrder(value = MethodOrderer.Random.class)
@Transactional
@Slf4j
public class OrderIntegrationTest {
    private static final UUID CUSTOMER_ID = UUID.fromString("f6316e90-1837-4940-b5db-a3c49a9a10ca");
    private static final UUID RESTAURANT_ID = UUID.fromString("ad68afcc-e55e-4e6a-bc6d-95a26a5410ff");
    private static final UUID ORDER_ID = UUID.fromString("f3b19e90-b5f3-4f68-bbfa-6aa1101ae3e1");
    private static final UUID ORDER_TRACKING_ID = UUID.fromString("4d510cac-290c-408a-a7fc-abc2f3c0efbb");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("cb48e255-cc1c-4fc3-b80c-c4d73ca187dd");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("d9e55ab9-68dc-4af5-b66f-a875b2df95fd");
    private static final Product PRODUCT_1 = Product.builder()
            .id(new ProductId(PRODUCT_ID_1))
            .name("product1")
            .price(Money.of(new BigDecimal("1000")))
            .build();
    private static final Product PRODUCT_2 = Product.builder()
            .id(new ProductId(PRODUCT_ID_2))
            .name("product2")
            .price(Money.of(new BigDecimal("3200")))
            .build();
    private static final com.sangjun.order.domain.entity.OrderItem ORDER_ITEM_1 = com.sangjun.order.domain.entity.OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .orderItemId(new OrderItemId(1L))
            .price(PRODUCT_1.getPrice())
            .quantity(2)
            .subTotal(PRODUCT_1.getPrice().multiply(2))
            .product(PRODUCT_1)
            .build();
    private static final com.sangjun.order.domain.entity.OrderItem ORDER_ITEM_2 = com.sangjun.order.domain.entity.OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .orderItemId(new OrderItemId(2L))
            .price(PRODUCT_2.getPrice())
            .quantity(1)
            .subTotal(PRODUCT_2.getPrice())
            .product(PRODUCT_2)
            .build();
    private static final RestaurantEntity RESTAURANT_ENTITY_1 = RestaurantEntity.builder()
            .restaurantId(RESTAURANT_ID)
            .restaurantActive(true)
            .restaurantName("restaurant")
            .productId(PRODUCT_ID_1)
            .productName("product1")
            .productPrice(PRODUCT_1.getPrice().getAmount())
            .productAvailable(true)
            .build();
    private static final RestaurantEntity RESTAURANT_ENTITY_2 = RestaurantEntity.builder()
            .restaurantId(RESTAURANT_ID)
            .restaurantActive(true)
            .restaurantName("restaurant")
            .productId(PRODUCT_ID_2)
            .productName("product2")
            .productPrice(PRODUCT_2.getPrice().getAmount())
            .productAvailable(true)
            .build();
    private static final OrderAddress ORDER_ADDRESS = OrderAddress.builder()
            .street("Sillim")
            .city("Seoul")
            .postalCode("4321")
            .build();
    private static final Order ORDER = Order.builder()
            .id(new OrderId(ORDER_ID))
            .customerId(new CustomerId(CUSTOMER_ID))
            .trackingId(new TrackingId(ORDER_TRACKING_ID))
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .orderStatus(OrderStatus.PENDING)
            .deliveryAddress(MAPPER.toStreetAddress(ORDER_ADDRESS))
            .failureMessages(new ArrayList<>())
            .items(List.of(ORDER_ITEM_1, ORDER_ITEM_2))
            .price(ORDER_ITEM_1.getSubTotal().add(ORDER_ITEM_2.getSubTotal()))
            .build();

    @Autowired
    private OrderApplicationService orderApplicationService;

    @MockBean
    private CustomerJpaRepository customerJpaRepository;

    @MockBean
    private RestaurantJpaRepository restaurantJpaRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ConsumerFactory<String, PaymentRequestAvroModel> paymentCf;

    @Autowired
    private ConsumerFactory<String, RestaurantApprovalRequestAvroModel> restaurantCf;

    @Autowired
    private KafkaTemplate<String, PaymentResponseAvroModel> paymentResponseKt;

    @Autowired
    private KafkaTemplate<String, RestaurantApprovalResponseAvroModel> restaurantResponseKt;

    @Value("${order-service.payment-request-topic-name}")
    private String paymentRequestTopic;

    @Value("${order-service.payment-response-topic-name}")
    private String paymentResponseTopic;

    @Value("${order-service.restaurant-approval-response-topic-name}")
    private String restaurantResponseTopic;

    @Value("${order-service.restaurant-approval-request-topic-name}")
    private String restaurantRequestTopic;

    private Consumer<String, PaymentRequestAvroModel> paymentRequestConsumer;
    private Consumer<String, RestaurantApprovalRequestAvroModel> restaurantRequestConsumer;

    @BeforeAll
    void setUp() throws Exception {
        paymentRequestConsumer = paymentCf.createConsumer("test-payment-request", "1");
        paymentRequestConsumer.subscribe(List.of(paymentRequestTopic));

        restaurantRequestConsumer = restaurantCf.createConsumer("test-restaurant-request", "1");
        restaurantRequestConsumer.subscribe(List.of(restaurantRequestTopic));

        Thread.sleep(1000);
    }

    @AfterAll
    void tearDown() {
        paymentRequestConsumer.close();
        restaurantRequestConsumer.close();
    }

    @AfterEach
    void cleanAfter() {
        readPaymentRequestRecords();
        readRestaurantRequestRecords();
        restaurantRequestConsumer.commitSync();
        paymentRequestConsumer.commitSync();
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
                        "TABLE_SCHEMA in ('p_order', 'customer', 'restaurant', 'payment')");
        List<String> statements = query.getResultList();

        for (String statement : statements) {
            query = entityManager.createNativeQuery(statement);
            query.executeUpdate();
        }

        query = entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE");
        query.executeUpdate();
    }


    @Test
    void 주문이_성공하면_추적번호가_발급된다() {
        //given
        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = getOrderItem(PRODUCT_1, 2);
        OrderItem item2 = getOrderItem(PRODUCT_2, 1);
        items.addAll(List.of(item1, item2));

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(item1.getSubTotal().add(item2.getSubTotal()))
                .items(items)
                .orderAddress(ORDER_ADDRESS)
                .build();

        //when
        mockCustomerFindById();
        mockFindByRestaurantIdAndProductIdIn();
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);

        //then
        assertNotNull(response.getOrderTrackingId());
        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
        assertEquals(response.getOrderTrackingId(), order.getTrackingId().getValue());
    }

    @Test
    void 주문이_성공하면_주문상태가_PENDING이다() throws InterruptedException {
        //given
        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = getOrderItem(PRODUCT_1, 2);
        OrderItem item2 = getOrderItem(PRODUCT_2, 1);
        items.addAll(List.of(item1, item2));

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(item1.getSubTotal().add(item2.getSubTotal()))
                .items(items)
                .orderAddress(ORDER_ADDRESS)
                .build();

        //when
        mockCustomerFindById();
        mockFindByRestaurantIdAndProductIdIn();
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        Thread.sleep(100);

        //then
        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    }

    @Test
    void 주문이_성공하면_결제요청_메세지가_날아간다() throws InterruptedException {
        //given
        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = getOrderItem(PRODUCT_1, 2);
        OrderItem item2 = getOrderItem(PRODUCT_2, 1);
        items.addAll(List.of(item1, item2));

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(item1.getSubTotal().add(item2.getSubTotal()))
                .items(items)
                .orderAddress(ORDER_ADDRESS)
                .build();

        //when
        mockCustomerFindById();
        mockFindByRestaurantIdAndProductIdIn();
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        Thread.sleep(100);

        //then
        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));

        Map<String, PaymentRequestAvroModel> resultTable = readPaymentRequestRecords();
        assertTrue(resultTable.containsKey(order.getId().getValue().toString()));

        PaymentRequestAvroModel record = resultTable.get(order.getId().getValue().toString());
        assertEquals(order.getId().getValue().toString(), record.getOrderId());
        assertEquals(PaymentOrderStatus.PENDING, record.getPaymentOrderStatus());
        assertEquals(order.getCustomerId().getValue().toString(), record.getCustomerId());
        assertEquals(order.getPrice().getAmount(), record.getPrice());

    }

    private void mockCustomerFindById() {
        CustomerEntity customerEntity = new CustomerEntity(CUSTOMER_ID);

        when(customerJpaRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customerEntity));
    }

    private void mockFindByRestaurantIdAndProductIdIn() {
        when(restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(RESTAURANT_ID, List.of(PRODUCT_ID_1, PRODUCT_ID_2)))
                .thenReturn(Optional.of(List.of(RESTAURANT_ENTITY_1, RESTAURANT_ENTITY_2)));
    }


    private OrderItem getOrderItem(Product product, int quantity) {
        return OrderItem.builder()
                .productId(product.getId().getValue())
                .quantity(quantity)
                .price(product.getPrice().getAmount())
                .subTotal(product.getPrice().multiply(quantity).getAmount())
                .build();
    }

    @Test
    void 주문추적이_성공한다() {
        orderRepository.save(ORDER);
        entityManager.flush();

        TrackOrderQuery query = TrackOrderQuery.builder()
                .orderTrackingId(ORDER_TRACKING_ID)
                .build();
        TrackOrderResponse response = orderApplicationService.trackOrder(query);
        assertNotNull(response.getOrderStatus());
        assertEquals(ORDER_TRACKING_ID, response.getOrderTrackingId());
    }

    @Test
    void 결제완료시_주문상태가_PAID로_변한다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        savePendingOrder();

        UUID paymentId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1200.00");

        PaymentResponseAvroModel response = getPaymentResponseAvroModel(paymentId, price);

        //when
        TestTransaction.start();
        paymentResponseKt.send(
                        paymentResponseTopic,
                        ORDER.getId().getValue().toString(),
                        response)
                .get();

        //then
        Thread.sleep(100);
        Order order = assertDoesNotThrow(() -> orderRepository.findById(ORDER.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order Not found")));

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
    }


    @Test
    void 결제완료시_식당승인요청_메세지를_전송한다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        savePendingOrder();

        UUID paymentId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1200.00");

        PaymentResponseAvroModel response = getPaymentResponseAvroModel(paymentId, price);

        //when
        TestTransaction.start();
        paymentResponseKt.send(
                        paymentResponseTopic,
                        ORDER_ID.toString(),
                        response)
                .get();

        //then
        Thread.sleep(100);
        Map<String, RestaurantApprovalRequestAvroModel> resultTable = readRestaurantRequestRecords();
        assertTrue(resultTable.containsKey(ORDER_ID.toString()));

        RestaurantApprovalRequestAvroModel record = resultTable.get(ORDER_ID.toString());
        assertEquals(ORDER_ID.toString(), record.getOrderId());
        assertEquals(ORDER.getPrice().getAmount(), record.getPrice());
        assertEquals(ORDER.getRestaurantId().getValue().toString(), record.getRestaurantId());
        ORDER.getItems().stream()
                .map(com.sangjun.order.domain.entity.OrderItem::getProduct)
                .map(Product::getId)
                .map(ProductId::getValue)
                .map(UUID::toString)
                .forEach(id ->
                        assertDoesNotThrow(() -> record.getProducts().stream()
                                .filter(p -> p.getId().equals(id))
                                .findFirst().orElseThrow()));
        assertEquals(RestaurantOrderStatus.PAID, record.getRestaurantOrderStatus());

    }

    private void savePendingOrder() {
        TestTransaction.flagForCommit();
        orderRepository.save(ORDER);
        entityManager.flush();
        TestTransaction.end();
    }

    private PaymentResponseAvroModel getPaymentResponseAvroModel(UUID paymentId, BigDecimal price) {
        PaymentResponseAvroModel response = PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setPaymentId(paymentId.toString())
                .setCreatedAt(Instant.now())
                .setCustomerId(CUSTOMER_ID.toString())
                .setFailureMessages(new ArrayList<>())
                .setSagaId("")
                .setPrice(price)
                .setPaymentStatus(PaymentStatus.COMPLETED)
                .build();
        return response;
    }

    @Test
    void 식당승인완료시_주문상태가_APPROVED로_변한다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        savePaidOrder();

        RestaurantApprovalResponseAvroModel response =
                getRestaurantApprovalResponseAvroModel(OrderApprovalStatus.APPROVED);

        //when
        TestTransaction.start();

        restaurantResponseKt.send(
                restaurantResponseTopic,
                ORDER_ID.toString(),
                response
        ).get();

        //then
        Thread.sleep(100);
        Order order = assertDoesNotThrow(() -> orderRepository.findById(ORDER.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order Not found")));

        assertEquals(OrderStatus.APPROVED, order.getOrderStatus());
    }

    private static RestaurantApprovalResponseAvroModel getRestaurantApprovalResponseAvroModel(OrderApprovalStatus approved) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setSagaId("")
                .setFailureMessages(new ArrayList<>())
                .setOrderApprovalStatus(approved)
                .setRestaurantId(RESTAURANT_ID.toString())
                .build();
    }

    @Test
    void 식당승인취소시_주문상태가_CANCELLING으로_변한다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        savePaidOrder();

        RestaurantApprovalResponseAvroModel response =
                getRestaurantApprovalResponseAvroModel(OrderApprovalStatus.REJECTED);

        //when
        TestTransaction.start();

        restaurantResponseKt.send(
                restaurantResponseTopic,
                ORDER_ID.toString(),
                response
        ).get();

        //then
        Thread.sleep(100);

        Order order = assertDoesNotThrow(() -> orderRepository.findById(ORDER.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order Not found")));

        assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
    }

    @Test
    void 식당주문취소시_결제취소_메세지가_날아간다() throws InterruptedException, ExecutionException {
        //given
        savePaidOrder();

        RestaurantApprovalResponseAvroModel response =
                getRestaurantApprovalResponseAvroModel(OrderApprovalStatus.REJECTED);

        //when
        TestTransaction.start();

        restaurantResponseKt.send(
                restaurantResponseTopic,
                ORDER_ID.toString(),
                response
        ).get();

        //then
        Thread.sleep(100);

        Map<String, PaymentRequestAvroModel> resultTable = readPaymentRequestRecords();
        assertTrue(resultTable.containsKey(ORDER_ID.toString()));

        PaymentRequestAvroModel record = resultTable.get(ORDER_ID.toString());
        assertEquals(PaymentOrderStatus.CANCELLED, record.getPaymentOrderStatus());
        assertEquals(ORDER_ID.toString(), record.getOrderId());
        assertEquals(ORDER.getPrice().getAmount(), record.getPrice());
        assertEquals(ORDER.getCustomerId().getValue().toString(), record.getCustomerId());
    }

    private void savePaidOrder() {
        TestTransaction.flagForCommit();

        Order savedOrder = orderRepository.save(ORDER);
        savedOrder.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(savedOrder);
        entityManager.flush();

        TestTransaction.end();
    }

    private Map<String, PaymentRequestAvroModel> readPaymentRequestRecords() {
        ConsumerRecords<String, PaymentRequestAvroModel> result = KafkaTestUtils.getRecords(paymentRequestConsumer, 100);
        Map<String, PaymentRequestAvroModel> resultTable = new HashMap<>();
        result.forEach(rec -> resultTable.put(rec.key(), rec.value()));
        paymentRequestConsumer.commitSync();
        return resultTable;
    }

    private Map<String, RestaurantApprovalRequestAvroModel> readRestaurantRequestRecords() {
        ConsumerRecords<String, RestaurantApprovalRequestAvroModel> result = KafkaTestUtils.getRecords(restaurantRequestConsumer, 100);
        Map<String, RestaurantApprovalRequestAvroModel> resultTable = new HashMap<>();
        result.forEach(rec -> resultTable.put(rec.key(), rec.value()));
        restaurantRequestConsumer.commitSync();
        return resultTable;
    }

}
