package com.sangjun.restaurant.dataaccess.mapper;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.*;
import com.sangjun.restaurant.dataaccess.entity.OrderApprovalEntity;
import com.sangjun.restaurant.domain.entity.OrderApproval;
import com.sangjun.restaurant.domain.entity.OrderDetail;
import com.sangjun.restaurant.domain.entity.Product;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.valueobject.OrderApprovalId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RestaurantDataAccessMapper {

    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant
                .getOrderDetail()
                .getProducts()
                .stream()
                .map(BaseEntity::getId)
                .map(BaseId::getValue)
                .toList();
    }

    public Restaurant restaurantEntitiesToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = restaurantEntities
                .stream()
                .findFirst()
                .orElseThrow(() -> new RestaurantDataAccessException("No restaurants found"));

        List<Product> products = restaurantEntities
                .stream()
                .map(entity ->
                        Product.builder()
                                .id(new ProductId(entity.getProductId()))
                                .name(entity.getProductName())
                                .price(new Money(entity.getProductPrice()))
                                .available(entity.getProductAvailable())
                                .build())
                .toList();

        return Restaurant.builder(
                        OrderDetail.builder(products)
                                .build())
                .id(new RestaurantId(restaurantEntity.getRestaurantId()))
                .active(restaurantEntity.getRestaurantActive())
                .build();
    }

    public OrderApprovalEntity orderApprovalToOrderApprovalEntity(OrderApproval orderApproval) {
        return OrderApprovalEntity.builder()
                .id(orderApproval.getId().getValue())
                .restaurantId(orderApproval.getRestaurantId().getValue())
                .orderId(orderApproval.getOrderId().getValue())
                .status(orderApproval.getOrderApprovalStatus())
                .build();
    }

    public OrderApproval orderApprovalEntityToOrderApproval(OrderApprovalEntity orderApproval) {
        return OrderApproval.builder(
                        new RestaurantId(orderApproval.getRestaurantId()),
                        new OrderId(orderApproval.getOrderId()),
                        orderApproval.getStatus())
                .id(new OrderApprovalId(orderApproval.getId()))
                .build();
    }
}
