package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;

import java.util.List;

public class OrderDetail extends BaseEntity<OrderId> {
    private OrderStatus orderStatus;
    private Money totalAmount;
    private final List<Product> products;

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setTotalAmount(Money totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public List<Product> getProducts() {
        return products;
    }

    private OrderDetail(Builder builder) {
        setId(builder.id);
        orderStatus = builder.orderStatus;
        totalAmount = builder.totalAmount;
        products = builder.products;
    }

    public static Builder builder(List<Product> products) {
        return new Builder(products);
    }


    public static final class Builder {
        private OrderId id;
        private OrderStatus orderStatus;
        private Money totalAmount;
        private final List<Product> products;

        private Builder(List<Product> products) {
            this.products = products;
        }

        public Builder id(OrderId val) {
            id = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder totalAmount(Money val) {
            totalAmount = val;
            return this;
        }

        public OrderDetail build() {
            return new OrderDetail(this);
        }
    }
}
