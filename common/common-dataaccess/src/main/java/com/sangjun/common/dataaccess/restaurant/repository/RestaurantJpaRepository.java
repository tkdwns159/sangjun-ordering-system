package com.sangjun.common.dataaccess.restaurant.repository;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantJpaRepository {

    Optional<List<RestaurantEntity>> findByRestaurantIdAndProductIdIn(UUID restaurantId, List<UUID> productIds);
}
