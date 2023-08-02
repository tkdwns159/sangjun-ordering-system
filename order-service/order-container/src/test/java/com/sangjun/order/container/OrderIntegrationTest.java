package com.sangjun.order.container;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.kafka.order.avro.model.PaymentStatus;
import com.sangjun.kafka.order.avro.model.*;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.service.dto.CancelOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddressDto;
import com.sangjun.order.domain.service.dto.create.OrderItemDto;
import com.sangjun.order.domain.service.ports.input.service.CancelOrderApplicationService;
import com.sangjun.order.domain.service.ports.input.service.CreateOrderApplicationService;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.valueobject.OrderItem;
import com.sangjun.order.domain.valueobject.Product;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
@RecordApplicationEvents
public class OrderIntegrationTest {
    private static final UUID CUSTOMER_ID = UUID.fromString("f6316e90-1837-4940-b5db-a3c49a9a10ca");
    private static final UUID RESTAURANT_ID = UUID.fromString("ad68afcc-e55e-4e6a-bc6d-95a26a5410ff");
    private static final UUID ORDER_TRACKING_ID = UUID.fromString("4d510cac-290c-408a-a7fc-abc2f3c0efbb");
    private static final UUID PRODUCT_ID_1 = UUID.fromString("cb48e255-cc1c-4fc3-b80c-c4d73ca187dd");
    private static final UUID PRODUCT_ID_2 = UUID.fromString("d9e55ab9-68dc-4af5-b66f-a875b2df95fd");
    private static final Product PRODUCT_1 = Product.builder()
            .id(new ProductId(PRODUCT_ID_1))
            .price(Money.of(new BigDecimal("1000")))
            .quantity(100)
            .build();
    private static final Product PRODUCT_2 = Product.builder()
            .id(new ProductId(PRODUCT_ID_2))
            .price(Money.of(new BigDecimal("3200")))
            .quantity(100)
            .build();
    private static final OrderItem ORDER_ITEM_1 = OrderItem.builder()
            .price(PRODUCT_1.getPrice())
            .quantity(2)
            .subTotal(PRODUCT_1.getPrice().multiply(2))
            .productId(PRODUCT_1.getId())
            .build();
    private static final OrderItem ORDER_ITEM_2 = OrderItem.builder()
            .price(PRODUCT_2.getPrice())
            .quantity(1)
            .subTotal(PRODUCT_2.getPrice())
            .productId(PRODUCT_2.getId())
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
    private static final OrderAddressDto ORDER_ADDRESS = OrderAddressDto.builder()
            .street("Sillim")
            .city("Seoul")
            .postalCode("4321")
            .build();
    private static final Order ORDER = Order.builder()
            .customerId(new CustomerId(CUSTOMER_ID))
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .deliveryAddress(MAPPER.toStreetAddress(ORDER_ADDRESS))
            .failureMessages(new ArrayList<>())
            .items(List.of(ORDER_ITEM_1, ORDER_ITEM_2))
            .price(ORDER_ITEM_1.getSubTotal().add(ORDER_ITEM_2.getSubTotal()))
            .build();

    @Autowired
    private OrderApplicationService orderApplicationService;
    @Autowired
    private CreateOrderApplicationService createOrderService;
    @Autowired
    private CancelOrderApplicationService cancelOrderService;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private ApplicationEvents applicationEvents;

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
    void setFixtures() {
        ORDER.initialize();
    }


    @BeforeAll
    void setUp() throws Exception {
        log.info("setting up consumers");
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
    void 주문_성공() {
        //given
        Money totalPrice = ORDER_ITEM_1.getSubTotal().add(ORDER_ITEM_2.getSubTotal());
        OrderItemDto orderItemDto1 = createOrderItemDto(ORDER_ITEM_1);
        OrderItemDto orderItemDto2 = createOrderItemDto(ORDER_ITEM_2);
        List<OrderItemDto> items = new ArrayList<>(Arrays.asList(orderItemDto1, orderItemDto2));
        OrderAddressDto orderAddressDto = OrderAddressDto.builder()
                .city("Seoul")
                .postalCode("12345")
                .street("Sillim")
                .build();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(totalPrice.getAmount())
                .items(items)
                .orderAddressDto(orderAddressDto)
                .build();
        //when
        when(restaurantRepository.findProductsByRestaurantIdInProductIds(any(), anyList()))
                .thenReturn(List.of(PRODUCT_1, PRODUCT_2));
        mockCustomerFindById();

        CreateOrderResponse resp = createOrderService.createOrder(command);

        //then
        Order createdOrder = orderRepository.findByTrackingId(new TrackingId(resp.getOrderTrackingId())).get();
        생성된_주문데이터_확인(totalPrice.getAmount(), orderAddressDto, items, createdOrder);
        결제요청_이벤트가_발행됨(createdOrder);
    }

    private void mockCustomerFindById() {
        Customer customer = new Customer(new CustomerId(CUSTOMER_ID));

        when(customerRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
    }

    private OrderItemDto createOrderItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .price(orderItem.getPrice().getAmount())
                .subTotal(orderItem.getSubTotal().getAmount())
                .productId(orderItem.getProductId().getValue())
                .quantity(orderItem.getQuantity())
                .build();
    }

    private void 생성된_주문데이터_확인(BigDecimal price,
                              OrderAddressDto orderAddressDto,
                              List<OrderItemDto> itemDtoList,
                              Order createdOrder) {
        assertThat(createdOrder.getOrderStatus())
                .isEqualTo(OrderStatus.PENDING);
        assertThat(createdOrder.getPrice())
                .isEqualTo(Money.of(price));
        assertThat(createdOrder.getCustomerId().getValue())
                .isEqualTo(CUSTOMER_ID);
        assertThat(createdOrder.getRestaurantId().getValue())
                .isEqualTo(RESTAURANT_ID);
        assertThat(createdOrder.getDeliveryAddress())
                .isEqualTo(StreetAddress.builder()
                        .city(orderAddressDto.getCity())
                        .postalCode(orderAddressDto.getPostalCode())
                        .street(orderAddressDto.getStreet())
                        .build());

        for (int i = 0; i < itemDtoList.size(); i++) {
            checkOrderItem(itemDtoList.get(i), createdOrder.getId(), createdOrder.getItems().get(i));
        }
    }

    private void 결제요청_이벤트가_발행됨(Order order) {
        Map<String, PaymentRequestAvroModel> map = readPaymentRequestRecords();
        UUID orderId = order.getId().getValue();
        PaymentRequestAvroModel msg = map.get(orderId.toString());

        assertThat(msg.getOrderId())
                .isEqualTo(orderId.toString());
        assertThat(msg.getPaymentOrderStatus())
                .isEqualTo(PaymentOrderStatus.PENDING);
        assertThat(msg.getPrice())
                .isEqualTo(order.getPrice().getAmount());
        assertThat(msg.getCustomerId())
                .isEqualTo(order.getCustomerId().getValue().toString());
        assertThat(msg.getRestaurantId())
                .isEqualTo(order.getRestaurantId().getValue().toString());
    }


    private void checkOrderItem(OrderItemDto orderItemDto, OrderId createdOrderId, OrderItem orderItem) {
        assertThat(orderItem.getId().getOrderId())
                .isEqualTo(createdOrderId);
        assertThat(orderItem.getProductId().getValue())
                .isEqualTo(orderItemDto.getProductId());
        assertThat(orderItem.getQuantity())
                .isEqualTo(orderItemDto.getQuantity());
        assertThat(orderItem.getPrice())
                .isEqualTo(Money.of(orderItemDto.getPrice()));
    }

    @Test
    void 결제요청에대한_완료메세지_처리() throws InterruptedException {
        //given
        orderRepository.save(ORDER);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        OrderId orderId = ORDER.getId();
        UUID paymentId = UUID.randomUUID();
        PaymentResponseAvroModel msg = PaymentResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderId.getValue().toString())
                .setRestaurantId(RESTAURANT_ID.toString())
                .setCustomerId(CUSTOMER_ID.toString())
                .setPaymentId(paymentId.toString())
                .setPaymentStatus(PaymentStatus.COMPLETED)
                .setSagaId("")
                .setPrice(ORDER.getPrice().getAmount())
                .setCreatedAt(Instant.now())
                .setFailureMessages(Collections.emptyList())
                .build();

        //when
        paymentResponseKt.send(paymentResponseTopic, orderId.getValue().toString(), msg);
        Thread.sleep(200);

        //then
        Order foundOrder = orderRepository.findById(orderId).get();
        assertThat(foundOrder.getOrderStatus())
                .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void 주문_취소() {
        //given
        orderRepository.save(ORDER);

        //when
        CancelOrderCommand command = CancelOrderCommand.builder()
                .orderTrackingId(ORDER.getTrackingId().getValue())
                .customerId(CUSTOMER_ID)
                .build();

        cancelOrderService.cancelOrder(command);

        //then
        Order foundOrder = orderRepository.findByTrackingId(new TrackingId(command.getOrderTrackingId())).get();
        assertThat(foundOrder.getOrderStatus())
                .isEqualTo(OrderStatus.CANCELLING);
        주문취소_이벤트가_발행됨();
    }

    /**
     * Todo
     * 주문취소를 완료하기위해 주문취소 이벤트를 구독하여 다른 BoundedContext에 메세지를 보내도록 구현
     */
    private void 주문취소_이벤트가_발행됨() {
        var cancellingEventList = applicationEvents.stream()
                .filter(event -> event instanceof OrderCancellingEvent)
                .toList();
        assertThat(cancellingEventList)
                .hasSize(1);
    }

    private Map<String, PaymentRequestAvroModel> readPaymentRequestRecords() {
        ConsumerRecords<String, PaymentRequestAvroModel> result =
                KafkaTestUtils.getRecords(paymentRequestConsumer, 100);
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
