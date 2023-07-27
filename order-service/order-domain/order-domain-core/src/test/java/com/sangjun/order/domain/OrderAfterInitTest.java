package com.sangjun.order.domain;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.OrderItem;
import com.sangjun.order.domain.valueobject.Product;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderAfterInitTest {
    OrderDomainService orderDomainService = new OrderDomainServiceImpl();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID TRACKING_ID = UUID.randomUUID();
    private static final List<Product> PRODUCTS = new ArrayList<>();

    private static final List<OrderItem> ORDER_ITEMS = new ArrayList<>();
    private static Money totalItemPrice;
    private final Order order = Order.builder()
            .id(new OrderId(ORDER_ID))
            .customerId(new CustomerId(CUSTOMER_ID))
            .trackingId(new TrackingId(TRACKING_ID))
            .items(ORDER_ITEMS)
            .orderStatus(OrderStatus.PENDING)
            .price(totalItemPrice)
            .build();

    @BeforeAll
    static void init() {
        Product product1 = Product.builder()
                .id(new ProductId(UUID.randomUUID()))
                .name("t1")
                .price(new Money(new BigDecimal("3000")))
                .build();

        Product product2 = Product.builder()
                .id(new ProductId(UUID.randomUUID()))
                .name("t2")
                .price(new Money(new BigDecimal("4000")))
                .build();

        PRODUCTS.add(product1);
        PRODUCTS.add(product2);

        ORDER_ITEMS.add(OrderItem.builder()
                .orderItemId(new OrderItemId(1L))
                .product(new Product(new ProductId(product1.getId().getValue())))
                .price(product1.getPrice())
                .subTotal(product1.getPrice().multiply(2))
                .quantity(2)
                .build());
        ORDER_ITEMS.add(OrderItem.builder()
                .orderItemId(new OrderItemId(2L))
                .product(new Product(new ProductId(product2.getId().getValue())))
                .price(product2.getPrice())
                .subTotal(product2.getPrice().multiply(1))
                .quantity(1)
                .build());

        totalItemPrice = ORDER_ITEMS.stream()
                .map(OrderItem::getSubTotal)
                .reduce(Money.ZERO, Money::add);
    }

    @Test
    void 주문결제시_OrderStatus가_PENDING이_아니면_예외가_발생한다() {
        order.setOrderStatus(OrderStatus.PAID);

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.payOrder(order));

        order.setOrderStatus(OrderStatus.PENDING);
    }

    @Test
    void 주문결제시_OrderStatus가_PAID로_변경된다() {
        order.setOrderStatus(OrderStatus.PENDING);
        orderDomainService.payOrder(order);
        assertEquals(OrderStatus.PAID, order.getOrderStatus());
    }

    @Test
    void 주문승인시_OrderStatus가_PAID가_아니면_예외가_발생한다() {
        order.setOrderStatus(OrderStatus.PENDING);

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.approveOrder(order));
    }

    @Test
    void 주문승인시_OrderStatus가_APPROVED로_변경된다() {
        order.setOrderStatus(OrderStatus.PAID);
        orderDomainService.approveOrder(order);

        assertEquals(OrderStatus.APPROVED, order.getOrderStatus());
    }

    @Test
    void 주문취소_시작시_OrderStatus가_PAID가_아니면_예외가_발생한다() {
        order.setOrderStatus(OrderStatus.PENDING);

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.initiateOrderCancel(order, new ArrayList<>()));
    }

    @Test
    void 주문취소_시작시_OrderStatus가_PAID이면_CANCELLING으로_변경된다() {
        order.setOrderStatus(OrderStatus.PAID);
        orderDomainService.initiateOrderCancel(order, new ArrayList<>());

        assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
    }

    @Test
    void 주문취소시_OrderStatus가_CANCELLING_이면_예외가_발생한다() {
        order.setOrderStatus(OrderStatus.CANCELLING);

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.cancelOrder(order, new ArrayList<>()));
    }

    @Test
    void 주문취소시_OrderStatus가_PENDING_이면_예외가_발생한다() {
        order.setOrderStatus(OrderStatus.PENDING);

        assertThrows(OrderDomainException.class,
                () -> orderDomainService.cancelOrder(order, new ArrayList<>()));
    }
}
