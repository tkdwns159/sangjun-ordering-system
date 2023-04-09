package com.sangjun.order.domain.event;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.order.domain.entity.Order;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public abstract class OrderEvent implements DomainEvent<Order> {
    private final Order order;
    private final ZonedDateTime createdAt;

    public OrderEvent(Order order, ZonedDateTime createdAt) {
        this.order = order;
        this.createdAt = createdAt;
    }
}
