package com.sangjun.restaurant.dataaccess.adapter;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.dataaccess.restaurant.repository.RestaurantJpaRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.dataaccess.mapper.RestaurantDataAccessMapper;
import com.sangjun.restaurant.domain.entity.Restaurant;
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
        List<UUID> productIds = restaurantDataAccessMapper.restaurantToRestaurantProducts(restaurant);
        return restaurantJpaRepository
                .findByRestaurantIdAndProductIdIn(restaurant.getId().getValue(), productIds)
                .map(restaurantDataAccessMapper::restaurantEntitiesToRestaurant);
    }
}
