package com.sangjun.order.container;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.kafka.order.avro.model.OrderApprovalStatus;
import com.sangjun.kafka.order.avro.model.PaymentStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@SpringBootTest(classes = OrderIntegrationTestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        count = 3,
        partitions = 3,
        bootstrapServersProperty = "kafka-config.bootstrap-servers",
        zookeeperPort = 2182,
        zkSessionTimeout = 3000,
        zkConnectionTimeout = 3000
)
@Transactional
@Slf4j
public class OrderIntegrationTest {
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID ORDER_TRACKING_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID_1 = UUID.randomUUID();
    private static final UUID PRODUCT_ID_2 = UUID.randomUUID();
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
    private ConsumerFactory<String, PaymentRequestAvroModel> consumerFactory;

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

    @BeforeEach
    void cleanUp() {
        // transaction 자동시작
        TestTransaction.flagForCommit();
        truncateAllTables();
        TestTransaction.end();

        TestTransaction.start();
        // 다음 수행할 함수를 위해 transaction 재시작
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
    void 주문이_성공하면_주문상태가_PENDING이다() {
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
        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
    }

    @Test
    void 주문이_성공하면_결제요청_메세지가_날아간다() {
        //given
        Consumer<String, PaymentRequestAvroModel> consumer =
                consumerFactory.createConsumer("test-payment-request", "1");
        consumer.subscribe(List.of(paymentRequestTopic));

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
        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));

        Map<String, PaymentRequestAvroModel> resultTable = consumeFromAllPartitions(consumer);

        assertTrue(resultTable.containsKey(order.getId().getValue().toString()));
    }

    private static Map<String, PaymentRequestAvroModel> consumeFromAllPartitions(Consumer<String, PaymentRequestAvroModel> consumer) {
        ConsumerRecords<String, PaymentRequestAvroModel> result = KafkaTestUtils.getRecords(consumer, 1000);
        Map<String, PaymentRequestAvroModel> resultTable = new HashMap<>();
        result.forEach(rec -> resultTable.put(rec.key(), rec.value()));
        return resultTable;
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
        TestTransaction.flagForCommit();
        orderRepository.save(ORDER);
        entityManager.flush();
        TestTransaction.end();

        UUID paymentId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1200.00");

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

        //when
        TestTransaction.start();
        paymentResponseKt.send(
                        paymentResponseTopic,
                        ORDER.getId().getValue().toString(),
                        response)
                .get(1, TimeUnit.SECONDS);

        //then
        Thread.sleep(500);
        Order order = assertDoesNotThrow(() -> orderRepository.findById(ORDER.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order Not found")));

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
    }

    @Test
    void 식당승인완료시_주문상태가_APPROVED로_변한다() throws ExecutionException, InterruptedException, TimeoutException {
        //given
        TestTransaction.flagForCommit();

        Order savedOrder = orderRepository.save(ORDER);
        savedOrder.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(savedOrder);
        entityManager.flush();

        TestTransaction.end();

        RestaurantApprovalResponseAvroModel response = RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(ORDER_ID.toString())
                .setCreatedAt(Instant.now())
                .setSagaId("")
                .setFailureMessages(new ArrayList<>())
                .setOrderApprovalStatus(OrderApprovalStatus.APPROVED)
                .setRestaurantId(RESTAURANT_ID.toString())
                .build();

        //when
        TestTransaction.start();

        restaurantResponseKt.send(
                restaurantResponseTopic,
                ORDER_ID.toString(),
                response
        ).get(500, TimeUnit.MILLISECONDS);

        //then
        Thread.sleep(500);
        Order order = assertDoesNotThrow(() -> orderRepository.findById(ORDER.getId())
                .orElseThrow(() -> new OrderNotFoundException("Order Not found")));

        assertEquals(OrderStatus.APPROVED, order.getOrderStatus());
    }

}
