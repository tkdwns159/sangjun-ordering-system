package com.sangjun.order.domain.event;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class OrderCancellingEvent extends ApplicationEvent {

    public OrderCancellingEvent(Object source) {
        super(source);
    }

    public OrderCancellingEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
