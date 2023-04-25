package com.sangjun.order.domain.service;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderAddress;
import com.sangjun.order.domain.service.dto.create.OrderItem;
import com.sangjun.order.domain.service.mapper.OrderDataMapper;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationTest {

    @Autowired
    private OrderDomainService orderDomainService;

    @Autowired
    private OrderDataMapper orderDataMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private OrderApplicationService orderApplicationService;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderCommand createOrderCommandWrongPrice;
    private CreateOrderCommand createOrderCommandWrongProductPrice;
    private final UUID CUSTOMER_ID = UUID.fromString("b35eddfa-236a-47fe-af37-371869879eeb");
    private final UUID RESTAURANT_ID = UUID.fromString("5f005065-fc7a-488d-9113-0272ecb09e14");
    private final UUID PRODUCT_ID = UUID.fromString("8f1599b8-7c02-466a-ae6b-7f465f60803b");
    private final UUID ORDER_ID = UUID.fromString("24428442-0186-4700-a6ba-6a8df60d7b59");
    private final BigDecimal PRICE = new BigDecimal("200.00");

    @BeforeAll
    void init() {
        createOrderCommand = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .price(PRICE)
                .orderAddress(OrderAddress.builder()
                        .city("Seoul")
                        .postalCode("44414")
                        .street("Pochun")
                        .build())
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();
        createOrderCommandWrongPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .orderAddress(OrderAddress.builder()
                        .city("Seoul")
                        .postalCode("44414")
                        .street("Pochun")
                        .build())
                .price(new BigDecimal("250.00"))
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("50.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .orderAddress(OrderAddress.builder()
                        .city("Seoul")
                        .postalCode("44414")
                        .street("Pochun")
                        .build())
                .price(new BigDecimal("210.00"))
                .items(List.of(OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(1)
                                .price(new BigDecimal("60.00"))
                                .subTotal(new BigDecimal("60.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(PRODUCT_ID)
                                .quantity(3)
                                .price(new BigDecimal("50.00"))
                                .subTotal(new BigDecimal("150.00"))
                                .build()))
                .build();

        Customer customer = new Customer();
        customer.setId(new CustomerId(CUSTOMER_ID));

        Restaurant restaurantResponse = Restaurant.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
                .active(true)
                .build();

        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        order.setId(new OrderId(ORDER_ID));

        when(customerRepository.findCustomer(CUSTOMER_ID))
                .thenReturn(Optional.of(customer));
        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
    }

    @Test
    void testCreateOrder() {
        CreateOrderResponse response = orderApplicationService.createOrder(createOrderCommand);
        assertEquals(response.getOrderStatus(), OrderStatus.PENDING);
        assertEquals(response.getMessage(), "Order Created Successfully");
        assertNotNull(response.getOrderTrackingId());
    }

    @Test
    void testCreateOrderWithWrongTotalPrice() {
        OrderDomainException exception = assertThrows(OrderDomainException.class, () ->
                orderApplicationService.createOrder(createOrderCommandWrongPrice)
        );

        assertEquals("Total price: " + createOrderCommandWrongPrice.getPrice()
                + " is not equal to Order items total: " + createOrderCommandWrongPrice.getItems().stream()
                .map(orderItem -> orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add) + "!", exception.getMessage());
    }

    @Test
    void testCrateOrderWithWrongProductPrice() {
        OrderDomainException exception = assertThrows(OrderDomainException.class, () ->
                orderApplicationService.createOrder(createOrderCommandWrongProductPrice)
        );

        assertEquals("Order item price: 60.00 is not valid for product " + PRODUCT_ID, exception.getMessage());
    }

    @Test
    void testCreateOrderWithPassiveRestaurant() {
        Restaurant restaurantResponse = Restaurant.builder()
                .id(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(List.of(new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
                        new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))))
                .active(false)
                .build();

        when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
                .thenReturn(Optional.of(restaurantResponse));

        OrderDomainException exception = assertThrows(OrderDomainException.class, () -> orderApplicationService.createOrder(createOrderCommand));

        assertEquals("Restaurant with id " + RESTAURANT_ID + " is currently not active!", exception.getMessage());
    }


}
