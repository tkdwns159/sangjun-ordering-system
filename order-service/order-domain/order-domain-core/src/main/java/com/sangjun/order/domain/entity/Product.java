package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Product {
    private ProductId id;
    private Money price;
    private int quantity;

    private Product(ProductId id, Money price, int quantity) {
        this.id = id;
        this.price = price;
        this.quantity = quantity;
    }

    private Product(Builder builder) {
        id = requireNonNull(builder.id, "productId");
        price = requireNonNull(builder.price, "price");
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

    public boolean hasSamePrice(Product product) {
        return this.price.equals(product.getPrice());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return quantity == product.quantity && Objects.equals(id, product.id) && Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, quantity);
    }
}

