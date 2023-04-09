package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.restaurant.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {
    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
