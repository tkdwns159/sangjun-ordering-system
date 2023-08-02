package com.sangjun.order.domain.valueobject;

import com.sangjun.common.domain.valueobject.OrderId;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Access(AccessType.FIELD)
public class OrderItemId implements Serializable {

    @Embedded
    private OrderId orderId;
    private Long orderItemId;

    public OrderItemId(OrderId orderId, Long orderItemId) {
        this.orderId = orderId;
        this.orderItemId = orderItemId;
    }

    protected OrderItemId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemId that = (OrderItemId) o;
        return Objects.equals(orderId, that.orderId) && Objects.equals(orderItemId, that.orderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, orderItemId);
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }
}
