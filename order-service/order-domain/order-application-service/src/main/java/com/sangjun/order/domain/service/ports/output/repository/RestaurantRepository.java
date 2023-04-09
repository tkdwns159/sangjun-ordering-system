package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.order.domain.entity.Restaurant;

import java.util.Optional;

public interface RestaurantRepository {

    Optional<Restaurant> findRestaurantInformation(Restaurant restaurant);
}
