package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import org.springframework.util.Assert;

import static java.util.Objects.requireNonNull;

public class Product {
    private ProductId id;
    private String name;
    private Money price;
    private int quantity;

    private Product(ProductId id, String name, Money price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    private Product(Builder builder) {
        validate(builder);
        id = builder.id;
        price = builder.price;
        quantity = builder.quantity;
        name = builder.name;
    }

    private void validate(Builder builder) {
        requireNonNull(builder.id, "productId");
        requireNonNull(builder.price, "price");
        Assert.hasText(builder.name, "name");
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

    public String getName() {
        return name;
    }

    public boolean hasSameName(Product product) {
        return this.name.equalsIgnoreCase(product.getName());
    }

    public boolean hasSamePrice(Product product) {
        return this.price.equals(product.getPrice());
    }

    public static final class Builder {
        private ProductId id;
        private Money price;
        private String name;
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

        public Builder name(String val) {
            name = val;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}

