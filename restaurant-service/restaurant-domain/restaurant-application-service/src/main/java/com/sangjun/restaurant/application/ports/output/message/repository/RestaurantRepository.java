package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.restaurant.domain.entity.Restaurant;

public interface RestaurantRepository {
    Restaurant save(Restaurant restaurant);
}
