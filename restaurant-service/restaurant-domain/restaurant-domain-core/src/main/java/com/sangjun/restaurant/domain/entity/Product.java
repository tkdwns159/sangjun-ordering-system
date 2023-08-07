package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "product", schema = "restaurant")
@Access(AccessType.FIELD)
public class Product extends BaseEntity<ProductId> {
    private String name;
    private Money price;
    private int quantity;
    private boolean available;

    public Money getPrice() {
        return price;
    }

    protected Product() {
    }

    private Product(Builder builder) {
        setId(new ProductId(UUID.randomUUID()));
        name = builder.name;
        price = builder.price;
        quantity = builder.quantity;
        available = builder.available;
    }

    public static Builder builder() {
        return new Builder();
    }


    public static final class Builder {
        private String name;
        private Money price;
        private int quantity;
        private boolean available;

        private Builder() {
        }

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder quantity(int val) {
            quantity = val;
            return this;
        }

        public Builder available(boolean val) {
            available = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
