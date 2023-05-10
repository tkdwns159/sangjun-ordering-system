package com.sangjun.order.domain.event;

import com.sangjun.order.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCancelledEvent extends OrderEvent {
    public OrderCancelledEvent(Order order, ZonedDateTime createAt) {
        super(order, createAt);
    }
}
