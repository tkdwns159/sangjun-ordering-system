package com.sangjun.order.domain.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddress;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.sangjun.common.domain.CommonConstants.ZONE_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
public class OrderCreateTest {
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final Customer CUSTOMER = new Customer(new CustomerId(CUSTOMER_ID));
    private static final Restaurant RESTAURANT = Restaurant.builder()
            .id(new RestaurantId(RESTAURANT_ID))
            .active(true)
            .products(new ArrayList<>())
            .build();

    private static final OrderAddress ORDER_ADDRESS = OrderAddress.builder()
            .city("Seoul")
            .postalCode("432")
            .street("Sillim")
            .build();

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    public OrderDomainService orderDomainService;

    @Mock
    public CustomerRepository customerRepository;

    @InjectMocks
    public OrderApplicationServiceImpl orderApplicationService;

    @BeforeEach
    void setUp() {
        RESTAURANT.setActive(true);
    }


    @Test
    void 고객번호를_찾을수없으면_예외가_발생한다() {
        UUID invalidCustomerId = UUID.randomUUID();
        CreateOrderCommand createCommand = CreateOrderCommand.builder()
                .customerId(invalidCustomerId)
                .build();

        assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createCommand));
    }

    @Test
    void 고객번호를_찾을수있으면_통과한다() {
        CreateOrderCommand createCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        when(customerRepository.findCustomer(CUSTOMER_ID))
                .thenReturn(Optional.of(CUSTOMER));
        when(restaurantRepository.findRestaurantInformation(RESTAURANT))
                .thenReturn(Optional.of(RESTAURANT));
        when(orderDomainService.initiateOrder(any(Order.class)))
                .thenReturn(new OrderCreatedEvent(Order.builder().build(), ZonedDateTime.now(ZoneId.of(ZONE_ID))));

        assertDoesNotThrow(() -> orderApplicationService.createOrder(createCommand));
    }

    @Test
    void 식당이_운영하지_않으면_예외가_발생한다() {
        RESTAURANT.setActive(false);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .build();

        assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 식당이_존재하지_않으면_예외가_발생한다() {
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(UUID.randomUUID())
                .build();

        assertThrows(OrderDomainException.class,
                () -> orderApplicationService.createOrder(createOrderCommand));
    }

    @Test
    void 주문이_성공적으로_생성된다() {
        Order order = Order.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .trackingId(new TrackingId(UUID.randomUUID()))
                .orderStatus(OrderStatus.PENDING)
                .build();
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)));

        when(orderDomainService.initiateOrder(any(Order.class)))
                .thenReturn(orderCreatedEvent);
        when(customerRepository.findCustomer(CUSTOMER_ID))
                .thenReturn(Optional.of(CUSTOMER));
        when(restaurantRepository.findRestaurantInformation(RESTAURANT))
                .thenReturn(Optional.of(RESTAURANT));

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .price(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .orderAddress(ORDER_ADDRESS)
                .build();

        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);

        assertNotNull(response.getOrderTrackingId());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
    }
}
