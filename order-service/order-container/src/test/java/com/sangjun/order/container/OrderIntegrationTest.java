package com.sangjun.order.container;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.common.domain.valueobject.*;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@ActiveProfiles("test")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderDataTestConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Slf4j
public class OrderIntegrationTest {
    public static final UUID CUSTOMER_ID = UUID.randomUUID();
    public static final UUID RESTAURANT_ID = UUID.randomUUID();
    public static final UUID ORDER_ID = UUID.randomUUID();
    public static final UUID ORDER_TRACKING_ID = UUID.randomUUID();
    public static final UUID PRODUCT_ID_1 = UUID.randomUUID();
    public static final UUID PRODUCT_ID_2 = UUID.randomUUID();
    public static final Product PRODUCT_1 = Product.builder()
            .id(new ProductId(PRODUCT_ID_1))
            .name("product1")
            .price(Money.of(new BigDecimal("1000")))
            .build();
    public static final Product PRODUCT_2 = Product.builder()
            .id(new ProductId(PRODUCT_ID_2))
            .name("product2")
            .price(Money.of(new BigDecimal("3200")))
            .build();
    public static final com.sangjun.order.domain.entity.OrderItem ORDER_ITEM_1 = com.sangjun.order.domain.entity.OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .orderItemId(new OrderItemId(1L))
            .price(PRODUCT_1.getPrice())
            .quantity(2)
            .subTotal(PRODUCT_1.getPrice().multiply(2))
            .product(PRODUCT_1)
            .build();
    public static final com.sangjun.order.domain.entity.OrderItem ORDER_ITEM_2 = com.sangjun.order.domain.entity.OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .orderItemId(new OrderItemId(2L))
            .price(PRODUCT_2.getPrice())
            .quantity(1)
            .subTotal(PRODUCT_2.getPrice())
            .product(PRODUCT_2)
            .build();
    public static final RestaurantEntity RESTAURANT_ENTITY_1 = RestaurantEntity.builder()
            .restaurantId(RESTAURANT_ID)
            .restaurantActive(true)
            .restaurantName("restaurant")
            .productId(PRODUCT_ID_1)
            .productName("product1")
            .productPrice(PRODUCT_1.getPrice().getAmount())
            .productAvailable(true)
            .build();
    public static final RestaurantEntity RESTAURANT_ENTITY_2 = RestaurantEntity.builder()
            .restaurantId(RESTAURANT_ID)
            .restaurantActive(true)
            .restaurantName("restaurant")
            .productId(PRODUCT_ID_2)
            .productName("product2")
            .productPrice(PRODUCT_2.getPrice().getAmount())
            .productAvailable(true)
            .build();
    public static final OrderAddress ORDER_ADDRESS = OrderAddress.builder()
            .street("Sillim")
            .city("Seoul")
            .postalCode("4321")
            .build();
    public static final Order ORDER = Order.builder()
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


    @AfterTransaction
    void clearContext() {
        entityManager.clear();
    }

    @Test
    void 주문이_성공한다() {
        //given
        CustomerEntity customerEntity = new CustomerEntity(CUSTOMER_ID);

        when(customerJpaRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(customerEntity));
        when(restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(RESTAURANT_ID, List.of(PRODUCT_ID_1, PRODUCT_ID_2)))
                .thenReturn(Optional.of(List.of(RESTAURANT_ENTITY_1, RESTAURANT_ENTITY_2)));

        List<OrderItem> items = new ArrayList<>();
        OrderItem item1 = getOrderItem(PRODUCT_1, 2);
        OrderItem item2 = getOrderItem(PRODUCT_2, 1);
        items.add(item1);
        items.add(item2);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(item1.getSubTotal().add(item2.getSubTotal()))
                .items(items)
                .orderAddress(ORDER_ADDRESS)
                .build();

        //when
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        entityManager.flush();

        //then
        assertNotNull(response.getOrderTrackingId());
        assertEquals("Order created successfully", response.getMessage());

        Order order = assertDoesNotThrow(() -> orderRepository.findByTrackingId(response.getOrderTrackingId())
                .orElseThrow(() -> new OrderNotFoundException("Order not found")));
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(response.getOrderTrackingId(), order.getTrackingId().getValue());
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
}
