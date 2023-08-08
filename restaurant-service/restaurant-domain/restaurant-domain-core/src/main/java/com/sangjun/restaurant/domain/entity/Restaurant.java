package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.valueobject.RestaurantId;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public List<Product> getProducts() {
        return new ArrayList<>(this.products);
    }

    protected Restaurant() {
    }

    private Restaurant(Builder builder) {
        super.setId(new RestaurantId(UUID.randomUUID()));
        products = builder.products;
        pendingOrders = builder.pendingOrders;
        isActive = builder.isActive;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<Product> products;
        private List<PendingOrder> pendingOrders;
        private boolean isActive;

        private Builder() {
        }

        public Builder products(List<Product> val) {
            products = val;
            return this;
        }

        public Builder pendingOrders(List<PendingOrder> val) {
            pendingOrders = val;
            return this;
        }

        public Builder isActive(boolean val) {
            isActive = val;
            return this;
        }

        public Restaurant build() {
            return new Restaurant(this);
        }
    }
}
