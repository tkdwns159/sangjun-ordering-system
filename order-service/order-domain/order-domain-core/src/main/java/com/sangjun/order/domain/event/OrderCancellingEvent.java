package com.sangjun.order.domain.event;

import com.sangjun.order.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCancellingEvent extends OrderEvent {

    public OrderCancellingEvent(Order order, ZonedDateTime createdAt) {
        super(order, createdAt);
    }
}
