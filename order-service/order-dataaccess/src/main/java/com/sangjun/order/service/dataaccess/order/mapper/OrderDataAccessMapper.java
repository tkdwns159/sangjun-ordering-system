package com.sangjun.order.service.dataaccess.order.mapper;

import com.sangjun.common.domain.CommonConstants;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.OrderItem;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import com.sangjun.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.sangjun.order.service.dataaccess.order.entity.OrderEntity;
import com.sangjun.order.service.dataaccess.order.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.sangjun.common.domain.CommonConstants.FAILURE_MESSAGE_DELIMITER;

@Component
public class OrderDataAccessMapper {


    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getId())
                .address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
                .price(order.getPrice().getAmount())
                .items(orderItemsToOrderItemEntities(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(
                        String.join(CommonConstants.FAILURE_MESSAGE_DELIMITER, Optional.ofNullable(order.getFailureMessages())
                                .orElse(Collections.emptyList())))
                .build();

        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().forEach(item -> item.setOrder(orderEntity));

        return orderEntity;
    }

    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
        return OrderAddressEntity.builder()
                .city(deliveryAddress.getCity())
                .id(deliveryAddress.getId())
                .postalCode(deliveryAddress.getPostalCode())
                .street(deliveryAddress.getStreet())
                .build();
    }

    private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
        return items.stream().map(item -> OrderItemEntity.builder()
                        .id(item.getId().getValue())
                        .price(item.getPrice().getAmount())
                        .quantity(item.getQuantity())
                        .subTotal(item.getSubTotal().getAmount())
                        .productId(item.getProduct().getId().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                .orderStatus(orderEntity.getOrderStatus())
                .price(new Money(orderEntity.getPrice()))
                .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
                .failureMessages(
                        Optional.ofNullable(orderEntity.getFailureMessages())
                                .map(s -> s.split(FAILURE_MESSAGE_DELIMITER))
                                .map(Arrays::asList)
                                .orElse(Collections.emptyList())
                )
                .build();
    }

    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
        return new StreetAddress(address.getId(), address.getStreet(), address.getPostalCode(), address.getCity());
    }

    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items.stream()
                .map(item -> OrderItem.builder()
                        .orderItemId(new OrderItemId(item.getId()))
                        .subTotal(new Money(item.getSubTotal()))
                        .price(new Money(item.getPrice()))
                        .quantity(item.getQuantity())
                        .product(new Product(new ProductId(item.getProductId())))
                        .build())
                .toList();
    }
}
