package com.sangjun.order.dataaccess.restaurant.mapper;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Mapper
@Component
public class RestaurantDataAccessMapper {
    public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
        return restaurant.getProducts().stream()
                .map(Product::getId)
                .map(ProductId::getValue)
                .toList();
    }

    public Restaurant restaurantEntitiesToRestaurant(List<RestaurantEntity> restaurantEntities) {
        RestaurantEntity restaurantEntity = restaurantEntities.stream().findFirst()
                .orElseThrow(() ->
                        new RestaurantDataAccessException("No restaurant found"));

        List<Product> products = restaurantEntities.stream()
                .map(entity ->
                        new Product(
                                new ProductId(entity.getProductId()),
                                entity.getProductName(),
                                new Money(entity.getProductPrice())
                        )
                )
                .toList();

        return Restaurant.builder()
                .id(new RestaurantId(restaurantEntity.getRestaurantId()))
                .active(restaurantEntity.getRestaurantActive())
                .products(products)
                .build();
    }
}
