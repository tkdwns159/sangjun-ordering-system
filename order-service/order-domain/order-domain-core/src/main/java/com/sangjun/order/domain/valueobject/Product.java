package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;

public class Product {
    private ProductId id;
    private Money price;
    private int quantity;

    public Product(ProductId id, Money price, int quantity) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }

    private Product(Builder builder) {
        id = builder.id;
        price = builder.price;
        quantity = builder.quantity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Money getPrice() {
        return price;
    }

    public ProductId getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public static final class Builder {
        private ProductId id;
        private Money price;

        private int quantity;

        private Builder() {
        }

        public Builder id(ProductId val) {
            id = val;
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

        public Product build() {
            return new Product(this);
        }
    }
}

