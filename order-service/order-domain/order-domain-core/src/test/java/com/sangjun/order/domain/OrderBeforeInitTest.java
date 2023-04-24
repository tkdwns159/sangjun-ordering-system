package com.sangjun.order.domain;

import com.sangjun.common.domain.exception.DomainException;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.OrderItem;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.StreetAddress;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

// 주문 프로세스를 시작하기 전의 로직에 대한 테스트
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderBeforeInitTest {
    OrderDomainService orderDomainService = new OrderDomainServiceImpl();
    private static final UUID RESTAURANT_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID STREET_ADDRESS_ID = UUID.randomUUID();
    private static final List<Product> PRODUCTS = new ArrayList<>();
    private static final Restaurant RESTAURANT = Restaurant.builder()
            .restaurantId(new RestaurantId(RESTAURANT_ID))
            .active(true)
            .products(PRODUCTS)
            .build();
    private static final StreetAddress STREET_ADDRESS = StreetAddress.builder()
            .id(STREET_ADDRESS_ID)
            .city("SEOUL")
            .postalCode("124213")
            .build();

    @BeforeAll
    static void init() {
        PRODUCTS.add(Product.builder()
                .id(new ProductId(UUID.randomUUID()))
                .name("t1")
                .price(new Money(new BigDecimal("3000")))
                .build());

        PRODUCTS.add(Product.builder()
                .id(new ProductId(UUID.randomUUID()))
                .name("t2")
                .price(new Money(new BigDecimal("4000")))
                .build());
    }

    @BeforeEach
    void setUp() {
        RESTAURANT.setActive(true);
    }

    @Test
    void ID가_존재하면_예외가_발생한다() {
        Order order = Order.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .build();

        Assertions.assertThrows(DomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문상태가_존재하면_예외가_발생한다() {
        Order order = Order.builder()
                .orderStatus(OrderStatus.PENDING)
                .build();

        Assertions.assertThrows(DomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문물품_총가격이_NULL이면_예외가_발생한다() {
        Order order = Order.builder()
                .build();

        Assertions.assertThrows(DomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문물품_총가격이_실제_주문물품_가격의_총합과_다르면_예외가_발생한다() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .price(Money.of(new BigDecimal("3000")))
                .subTotal(Money.of(new BigDecimal("3000")))
                .build());
        items.add(OrderItem.builder()
                .price(Money.of(new BigDecimal("4000")))
                .subTotal(Money.of(new BigDecimal("4000")))
                .build());

        Order order = Order.builder()
                .price(Money.of(new BigDecimal("10000")))
                .items(items)
                .build();

        Assertions.assertThrows(DomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문물품_총가격이_실제_주문물품_가격의_총합과_같으면_통과한다() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .price(Money.of(new BigDecimal("3000")))
                .subTotal(Money.of(new BigDecimal("6000")))
                .quantity(2)
                .build());
        items.add(OrderItem.builder()
                .price(Money.of(new BigDecimal("4000")))
                .subTotal(Money.of(new BigDecimal("4000")))
                .quantity(1)
                .build());

        Order order = Order.builder()
                .price(Money.of(new BigDecimal("10000")))
                .items(items)
                .build();

        Assertions.assertDoesNotThrow(order::validateOrder);
    }

    @Test
    void 주문물품의_부분총가격이_NULL이면_예외가_발생한다() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .price(Money.of(new BigDecimal("6000")))
                .subTotal(null)
                .build());

        Order order = Order.builder()
                .price(Money.of(new BigDecimal("6000")))
                .items(items)
                .build();

        Assertions.assertThrows(OrderDomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문물품의_단가가_NULL이면_예외가_발생한다() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .subTotal(Money.of(new BigDecimal("6000")))
                .price(null)
                .build());

        Order order = Order.builder()
                .price(Money.of(new BigDecimal("6000")))
                .items(items)
                .build();

        Assertions.assertThrows(OrderDomainException.class,
                order::validateOrder);
    }

    @Test
    void 주문물품_부분총가격이_실제_단가의_합과_다르면_예외가_발생한다() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .subTotal(Money.of(new BigDecimal("10000")))
                .quantity(4)
                .price(Money.of(new BigDecimal("3000")))
                .build());

        Order order = Order.builder()
                .price(Money.of(new BigDecimal("10000")))
                .items(items)
                .build();

        Assertions.assertThrows(OrderDomainException.class,
                order::validateOrder);
    }


    @Test
    void 주문물품이_식당에서_취급하는_것이면_통과한다() {
        Product product1 = PRODUCTS.get(0);
        int quantity = 2;
        Money subTotal = product1.getPrice().multiply(quantity);

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .orderItemId(new OrderItemId(1L))
                .product(new Product(new ProductId(product1.getId().getValue())))
                .price(product1.getPrice())
                .subTotal(subTotal)
                .quantity(quantity)
                .build());

        Order order = Order.builder()
                .items(items)
                .customerId(new CustomerId(CUSTOMER_ID))
                .price(subTotal)
                .build();

        assertDoesNotThrow(() ->
                orderDomainService.validateOrder(order, RESTAURANT));
    }

    @Test
    void 주문물품이_식당에서_취급하지_않는것이면_예외가_발생한다() {
        // 이름, 가격이 같은 새로운 Product
        Product product1 = Product.builder()
                .id(new ProductId(UUID.randomUUID()))
                .price(new Money(new BigDecimal("3000")))
                .name("t1")
                .build();
        int quantity = 2;
        Money subTotal = product1.getPrice().multiply(quantity);

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .orderItemId(new OrderItemId(1L))
                .product(new Product(new ProductId(product1.getId().getValue())))
                .price(product1.getPrice())
                .subTotal(subTotal)
                .quantity(quantity)
                .build());

        Order order = Order.builder()
                .items(items)
                .customerId(new CustomerId(CUSTOMER_ID))
                .price(subTotal)
                .build();

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.validateOrder(order, RESTAURANT));
    }

    @Test
    void 식당이_운영하지_않으면_예외가_발생한다() {
        RESTAURANT.setActive(false);

        Order order = Order.builder()
                .customerId(new CustomerId(CUSTOMER_ID))
                .build();

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.validateOrder(order, RESTAURANT));
    }

    @Test
    void 주문을_시작하면_주문관련_값들이_배속된다() {
        Product product1 = PRODUCTS.get(0);
        int quantity = 2;
        Money subTotal = product1.getPrice().multiply(quantity);

        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder()
                .orderItemId(new OrderItemId(1L))
                .product(new Product(new ProductId(product1.getId().getValue())))
                .price(product1.getPrice())
                .subTotal(subTotal)
                .quantity(quantity)
                .build());

        Order order = Order.builder()
                .customerId(new CustomerId(CUSTOMER_ID))
                .restaurantId(RESTAURANT.getId())
                .price(subTotal)
                .items(items)
                .deliveryAddress(STREET_ADDRESS)
                .build();

        Order initiatedOrder = orderDomainService
                .initiateOrder(order)
                .getOrder();


        assertNotNull(initiatedOrder.getId());
        assertNotNull(initiatedOrder.getTrackingId());
        assertEquals(OrderStatus.PENDING, initiatedOrder.getOrderStatus());
        initiatedOrder.getItems()
                .forEach(item -> {
                    assertNotNull(item.getId());
                    assertNotNull(item.getOrderId());
                });
    }
}
