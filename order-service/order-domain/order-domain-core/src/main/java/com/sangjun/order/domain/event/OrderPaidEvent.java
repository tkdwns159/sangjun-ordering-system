package com.sangjun.order.domain.event;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.entity.Order;

import java.time.ZonedDateTime;

public class OrderPaidEvent extends OrderEvent {
    private final DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher;

    public OrderPaidEvent(Order order, ZonedDateTime createdAt, DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher) {
        super(order, createdAt);
        this.orderPaidEventDomainEventPublisher = orderPaidEventDomainEventPublisher;
    }

    @Override
    public void fire() {
        orderPaidEventDomainEventPublisher.publish(this);
    }
}
