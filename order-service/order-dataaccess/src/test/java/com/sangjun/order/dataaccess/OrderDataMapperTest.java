package com.sangjun.order.dataaccess;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.dataaccess.order.entity.OrderAddressEntity;
import com.sangjun.order.dataaccess.order.entity.OrderEntity;
import com.sangjun.order.dataaccess.order.entity.OrderItemEntity;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.OrderItem;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.sangjun.order.dataaccess.order.mapper.OrderDataMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class OrderDataMapperTest {

    public static final UUID ORDER_ID = UUID.randomUUID();
    public static final Long ORDER_ITEM_ID_1 = 1L;
    public static final Long ORDER_ITEM_ID_2 = 2L;
    public static final UUID TRACKING_ID = UUID.randomUUID();
    public static final UUID CUSTOMER_ID = UUID.randomUUID();
    public static final UUID RESTAURANT_ID = UUID.randomUUID();
    public static final UUID PRODUCT_ID_1 = UUID.randomUUID();
    public static final UUID PRODUCT_ID_2 = UUID.randomUUID();
    public static final OrderItem ORDER_ITEM_1 = OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .product(Product.builder()
                    .id(new ProductId(PRODUCT_ID_1))
                    .build())
            .orderItemId(new OrderItemId(ORDER_ITEM_ID_1))
            .price(Money.of(new BigDecimal("3000")))
            .subTotal(Money.of(new BigDecimal("3000")))
            .quantity(1)
            .build();

    public static final OrderItem ORDER_ITEM_2 = OrderItem.builder()
            .orderId(new OrderId(ORDER_ID))
            .product(Product.builder()
                    .id(new ProductId(PRODUCT_ID_2))
                    .build())
            .orderItemId(new OrderItemId(ORDER_ITEM_ID_2))
            .price(Money.of(new BigDecimal("4300")))
            .subTotal(Money.of(new BigDecimal("8600")))
            .quantity(2)
            .build();

    @Test
    void testOrderItemToOrderItemEntity() {
        int quantity = ORDER_ITEM_1.getQuantity();
        Money price = ORDER_ITEM_1.getPrice();

        OrderItemEntity orderItemEntity = MAPPER.toOrderItemEntity(ORDER_ITEM_1);

        assertNull(orderItemEntity.getOrder());
        assertEquals(quantity, orderItemEntity.getQuantity());
        assertEquals(price.getAmount(), orderItemEntity.getPrice());
        assertEquals(price.multiply(quantity).getAmount(), orderItemEntity.getSubTotal());
        assertEquals(ORDER_ITEM_ID_1, orderItemEntity.getId());
    }

    @Test
    void testOrderToOrderEntity() {
        StreetAddress streetAddress = StreetAddress.builder()
                .id(UUID.randomUUID())
                .city("Seoul")
                .street("Sillim")
                .postalCode("4321")
                .build();
        Order order = Order.builder()
                .id(new OrderId(ORDER_ID))
                .trackingId(new TrackingId(TRACKING_ID))
                .customerId(new CustomerId(CUSTOMER_ID))
                .restaurantId(new RestaurantId(RESTAURANT_ID))
                .orderStatus(OrderStatus.PENDING)
                .deliveryAddress(streetAddress)
                .price(ORDER_ITEM_1.getSubTotal().add(ORDER_ITEM_2.getSubTotal()))
                .items(List.of(ORDER_ITEM_1, ORDER_ITEM_2))
                .failureMessages(List.of("I", "have"))
                .build();

        OrderEntity orderEntity = MAPPER.toOrderEntity(order);
        OrderItemEntity orderItemEntity1 = orderEntity.getItems().get(0);
        OrderItemEntity orderItemEntity2 = orderEntity.getItems().get(1);

        assertEquals(ORDER_ID, orderEntity.getId());
        assertEquals(TRACKING_ID, orderEntity.getTrackingId());
        assertEquals(CUSTOMER_ID, orderEntity.getCustomerId());
        assertEquals(RESTAURANT_ID, orderEntity.getRestaurantId());
        assertEquals(OrderStatus.PENDING, orderEntity.getOrderStatus());
        assertEquals(streetAddress.getId(), orderEntity.getAddress().getId());
        assertEquals(streetAddress.getCity(), orderEntity.getAddress().getCity());
        assertEquals(streetAddress.getStreet(), orderEntity.getAddress().getStreet());
        assertEquals(streetAddress.getPostalCode(), orderEntity.getAddress().getPostalCode());
        assertNull(orderEntity.getAddress().getOrder());
        assertEquals(2, orderEntity.getItems().size());
        assertEquals(ORDER_ITEM_ID_1, orderItemEntity1.getId());
        assertEquals(ORDER_ITEM_ID_2, orderItemEntity2.getId());
        assertEquals(ORDER_ITEM_1.getPrice().getAmount(), orderItemEntity1.getPrice());
        assertEquals(ORDER_ITEM_2.getPrice().getAmount(), orderItemEntity2.getPrice());
        assertEquals(ORDER_ITEM_1.getSubTotal().getAmount(), orderItemEntity1.getSubTotal());
        assertEquals(ORDER_ITEM_2.getSubTotal().getAmount(), orderItemEntity2.getSubTotal());
        assertEquals(ORDER_ITEM_1.getQuantity(), orderItemEntity1.getQuantity());
        assertEquals(ORDER_ITEM_2.getQuantity(), orderItemEntity2.getQuantity());
        assertEquals("I,have", orderEntity.getFailureMessages());
        assertEquals(ORDER_ITEM_1.getProduct().getId().getValue(), orderItemEntity1.getProductId());
        assertEquals(ORDER_ITEM_2.getProduct().getId().getValue(), orderItemEntity2.getProductId());
        assertEquals(ORDER_ID, orderItemEntity1.getOrder().getId());
        assertEquals(ORDER_ID, orderItemEntity2.getOrder().getId());
    }

    @Test
    void testOrderEntityToOrder() {
        OrderAddressEntity address = OrderAddressEntity.builder()
                .order(OrderEntity.builder()
                        .id(ORDER_ID)
                        .build())
                .id(UUID.randomUUID())
                .city("Seoul")
                .street("Sillim")
                .postalCode("4321")
                .build();
        OrderItemEntity orderItemEntity1 = MAPPER.toOrderItemEntity(ORDER_ITEM_1);
        OrderItemEntity orderItemEntity2 = MAPPER.toOrderItemEntity(ORDER_ITEM_2);

        OrderEntity orderEntity = OrderEntity.builder()
                .id(ORDER_ID)
                .trackingId(TRACKING_ID)
                .customerId(CUSTOMER_ID)
                .restaurantId(RESTAURANT_ID)
                .orderStatus(OrderStatus.PENDING)
                .address(address)
                .failureMessages("I,have")
                .items(List.of(orderItemEntity1, orderItemEntity2))
                .build();

        Order order = MAPPER.toOrder(orderEntity);
        OrderItem orderItem1 = order.getItems().get(0);
        OrderItem orderItem2 = order.getItems().get(1);

        assertEquals(ORDER_ID, order.getId().getValue());
        assertEquals(TRACKING_ID, order.getTrackingId().getValue());
        assertEquals(CUSTOMER_ID, order.getCustomerId().getValue());
        assertEquals(RESTAURANT_ID, order.getRestaurantId().getValue());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(address.getId(), order.getDeliveryAddress().getId());
        assertEquals(address.getCity(), order.getDeliveryAddress().getCity());
        assertEquals(address.getStreet(), order.getDeliveryAddress().getStreet());
        assertEquals(address.getPostalCode(), order.getDeliveryAddress().getPostalCode());
        assertEquals(2, order.getItems().size());
        assertEquals(ORDER_ITEM_ID_1, orderItem1.getId().getValue());
        assertEquals(ORDER_ITEM_ID_2, orderItem2.getId().getValue());
        assertEquals(ORDER_ITEM_1.getPrice().getAmount(), orderItem1.getPrice().getAmount());
        assertEquals(ORDER_ITEM_2.getPrice().getAmount(), orderItem2.getPrice().getAmount());
        assertEquals(ORDER_ITEM_1.getSubTotal().getAmount(), orderItem1.getSubTotal().getAmount());
        assertEquals(ORDER_ITEM_2.getSubTotal().getAmount(), orderItem2.getSubTotal().getAmount());
        assertEquals(ORDER_ITEM_1.getQuantity(), orderItem1.getQuantity());
        assertEquals(ORDER_ITEM_2.getQuantity(), orderItem2.getQuantity());
        assertEquals("I", order.getFailureMessages().get(0));
        assertEquals("have", order.getFailureMessages().get(1));
        assertEquals(orderItem1.getProduct().getId().getValue(), ORDER_ITEM_1.getProduct().getId().getValue());
        assertEquals(orderItem2.getProduct().getId().getValue(), ORDER_ITEM_2.getProduct().getId().getValue());

    }
}
