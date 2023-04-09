package com.sangjun.order.domain.event;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCancelledEvent extends OrderEvent {
    private final DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher;

    public OrderCancelledEvent(Order order, ZonedDateTime createdAt, DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCancelledEventDomainEventPublisher = orderCancelledEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderCancelledEventDomainEventPublisher.publish(this);
    }
}
