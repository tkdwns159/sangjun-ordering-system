package com.sangjun.order.domain.service;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = {OrderTestConfiguration.class})
public class OrderCreateTest {
//    private static final UUID CUSTOMER_ID = UUID.randomUUID();
//    private static final UUID RESTAURANT_ID = UUID.randomUUID();
//    private static final UUID PRODUCT_ID = UUID.randomUUID();
//    private static final Customer CUSTOMER = new Customer(new CustomerId(CUSTOMER_ID));
//
//    private static final Product PRODUCT = Product.builder()
//            .id(new ProductId(PRODUCT_ID))
//            .price(Money.of(new BigDecimal("1000")))
//            .build();
//    private static final Restaurant RESTAURANT = Restaurant.builder()
//            .id(new RestaurantId(RESTAURANT_ID))
//            .active(true)
//            .products(List.of(PRODUCT))
//            .build();
//    private static final OrderAddressDto ORDER_ADDRESS = OrderAddressDto.builder()
//            .city("Seoul")
//            .postalCode("432")
//            .street("Sillim")
//            .build();
//
//    private static final OrderItemDto ORDER_ITEM = OrderItemDto.builder()
//            .productId(PRODUCT_ID)
//            .price(PRODUCT.getPrice().getAmount())
//            .quantity(1)
//            .subTotal(PRODUCT.getPrice().getAmount())
//            .build();
//
//
//    @Autowired
//    private RestaurantRepository restaurantRepository;
//
//    @Autowired
//    private OrderDomainService orderDomainService;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private OrderApplicationService orderApplicationService;
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @BeforeEach
//    void setUp() {
//        RESTAURANT.setActive(true);
//    }
//
//    @Test
//    void 고객번호를_찾을수없으면_예외가_발생한다() {
//        UUID invalidCustomerId = UUID.randomUUID();
//        CreateOrderCommand createCommand = CreateOrderCommand.builder()
//                .customerId(invalidCustomerId)
//                .restaurantId(RESTAURANT_ID)
//                .price(BigDecimal.ZERO)
//                .orderAddressDto(ORDER_ADDRESS)
//                .items(List.of(ORDER_ITEM))
//                .build();
//
//        assertThrows(OrderDomainException.class,
//                () -> orderApplicationService.createOrder(createCommand));
//    }
//
//    @Test
//    void 고객번호를_찾을수있으면_통과한다() {
//        CreateOrderCommand createCommand = CreateOrderCommand.builder()
//                .restaurantId(RESTAURANT_ID)
//                .customerId(CUSTOMER_ID)
//                .price(new BigDecimal("1000"))
//                .items(List.of(ORDER_ITEM))
//                .orderAddressDto(ORDER_ADDRESS)
//                .build();
//
//        when(customerRepository.findById(CUSTOMER_ID))
//                .thenReturn(Optional.of(CUSTOMER));
//        when(restaurantRepository.findRestaurantInformation(RESTAURANT))
//                .thenReturn(Optional.of(RESTAURANT));
//
//        assertDoesNotThrow(() -> orderApplicationService.createOrder(createCommand));
//    }
//
//    @Test
//    void 식당이_존재하지_않으면_예외가_발생한다() {
//        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
//                .restaurantId(UUID.randomUUID())
//                .customerId(CUSTOMER_ID)
//                .price(BigDecimal.ZERO)
//                .items(List.of(ORDER_ITEM))
//                .orderAddressDto(ORDER_ADDRESS)
//                .build();
//
//        assertThrows(OrderDomainException.class,
//                () -> orderApplicationService.createOrder(createOrderCommand));
//    }
//
//    @Test
//    void 주문이_성공적으로_생성된다() {
//        UUID orderId = UUID.randomUUID();
//        UUID trackingId = UUID.randomUUID();
//        OrderItem orderItem = OrderItem.builder()
//                .price(PRODUCT.getPrice())
//                .quantity(1)
//                .subTotal(PRODUCT.getPrice())
//                .productId(new ProductId(PRODUCT_ID))
//                .build();
//
//        when(customerRepository.findById(CUSTOMER_ID))
//                .thenReturn(Optional.of(CUSTOMER));
//        when(restaurantRepository.findRestaurantInformation(RESTAURANT))
//                .thenReturn(Optional.of(RESTAURANT));
//        when(orderRepository.save(any(Order.class)))
//                .thenReturn(Order.builder()
//                        .restaurantId(new RestaurantId(RESTAURANT_ID))
//                        .customerId(new CustomerId(CUSTOMER_ID))
//                        .price(new Money(new BigDecimal("1000")))
//                        .deliveryAddress(OrderMapstructMapper.MAPPER.toStreetAddress(ORDER_ADDRESS))
//                        .items(List.of(orderItem))
//                        .build());
//
//        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
//                .restaurantId(RESTAURANT_ID)
//                .customerId(CUSTOMER_ID)
//                .price(new BigDecimal("1000"))
//                .items(List.of(ORDER_ITEM))
//                .orderAddressDto(ORDER_ADDRESS)
//                .build();
//
//        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
//
//        assertEquals(trackingId, response.getOrderTrackingId());
//        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
//        assertEquals("Order created successfully", response.getMessage());
//    }
}
