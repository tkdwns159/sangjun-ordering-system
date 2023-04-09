package com.sangjun.order.service.dataaccess.restaurant.adapter;

import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.service.dataaccess.restaurant.mapper.RestaurantDataAccessMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryImpl implements RestaurantRepository {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;

    @Override
    public Optional<Restaurant> findRestaurantInformation(Restaurant restaurant) {
        List<UUID> productsIds = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        return restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), productsIds)
                .map(restaurantDataAccessMapper::restaurantEntitiesToRestaurant);
    }
}
