package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.RestaurantId;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "restaurants", schema = "restaurant")
@Access(AccessType.FIELD)
public class Restaurant extends AggregateRoot<RestaurantId> {

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.DETACH},
            orphanRemoval = true)
    @JoinColumn(name = "restaurant_id")
    private List<Product> products;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.DETACH},
            orphanRemoval = true)
    @JoinColumn(name = "restaurant_id")
    private List<PendingOrder> pendingOrders;

    private boolean isActive;

}
