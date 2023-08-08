package com.sangjun.restaurant.dataaccess.repository;

import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.domain.entity.Restaurant;
import org.springframework.data.repository.Repository;

public interface RestaurantJpaRepository extends RestaurantRepository, Repository<Restaurant, RestaurantId> {
    @Override
    Restaurant save(Restaurant restaurant);
}
