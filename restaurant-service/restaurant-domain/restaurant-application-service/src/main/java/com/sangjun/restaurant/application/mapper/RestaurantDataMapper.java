package com.sangjun.restaurant.application.mapper;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.domain.entity.OrderDetail;
import com.sangjun.restaurant.domain.entity.Product;
import com.sangjun.restaurant.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RestaurantDataMapper {
    public Restaurant restaurantApprovalRequestToRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        return Restaurant.builder(
                        OrderDetail.builder(restaurantApprovalRequest.getProducts().stream()
                                        .map(product -> Product.builder()
                                                .id(product.getId())
                                                .quantity(product.getQuantity())
                                                .build())
                                        .toList())
                                .id(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
                                .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
                                .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
                                .build()
                )
                .id(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
                .build();
    }
}
