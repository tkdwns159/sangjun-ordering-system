package com.sangjun.order.dataaccess.restaurant.mapper;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.valueobject.Product;
import com.sangjun.order.domain.valueobject.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RestaurantDataMapstructMapper {
    RestaurantDataMapstructMapper MAPPER = Mappers.getMapper(RestaurantDataMapstructMapper.class);

    @Mapping(target = "id.value", source = "productId")
    @Mapping(target = "name", source = "productName")
    @Mapping(target = "price.amount", source = "productPrice")
    Product toProduct(RestaurantEntity restaurantEntity);

    default Restaurant toRestaurant(List<RestaurantEntity> restaurantEntities) {
        if (restaurantEntities.isEmpty()) {
            throw new OrderDomainException("No restaurant found");
        }

        RestaurantEntity represent = restaurantEntities.get(0);

        return Restaurant.builder()
                .id(new RestaurantId(represent.getRestaurantId()))
                .products(restaurantEntities
                        .stream()
                        .map(this::toProduct)
                        .toList())
                .active(represent.getRestaurantActive())
                .build();
    }


}
