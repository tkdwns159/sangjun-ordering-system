package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.RestaurantId;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class Restaurant extends AggregateRoot<RestaurantId> {
    private final List<Product> products;
    private boolean active;

    @Builder
    public Restaurant(RestaurantId restaurantId, List<Product> products, boolean active) {
        super.setId(restaurantId);
        this.products = products;
        this.active = active;
    }

}
