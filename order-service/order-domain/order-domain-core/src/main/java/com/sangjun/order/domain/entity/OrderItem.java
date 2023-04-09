package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.order.domain.valueobject.OrderItemId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItem extends BaseEntity<OrderItemId> {
    private OrderId orderId;
    private final Product product;
    private final int quantity;
    private final Money price;
    private final Money subTotal;

    boolean isPriceValid() {
        return this.price.isGreaterThanZero() &&
                this.price.equals(product.getPrice()) &&
                this.price.multiply(this.quantity).equals(subTotal);
    }

    @Builder
    public OrderItem(OrderItemId orderItemId, Product product, int quantity, Money price, Money subTotal) {
        super.setId(orderItemId);
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.subTotal = subTotal;
    }

    void initializeOrderItem(OrderId orderId, OrderItemId orderItemId) {
        this.orderId = orderId;
        super.setId(orderItemId);
    }
}
