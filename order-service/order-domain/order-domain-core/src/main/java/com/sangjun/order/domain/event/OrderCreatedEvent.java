package com.sangjun.order.domain.event;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderCreatedEvent extends OrderEvent {
    private final DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher;

    public OrderCreatedEvent(Order order, ZonedDateTime createdAt, DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderCreatedEventDomainEventPublisher = orderCreatedEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderCreatedEventDomainEventPublisher.publish(this);
    }
}
