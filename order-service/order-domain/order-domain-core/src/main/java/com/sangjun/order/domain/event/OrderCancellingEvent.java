package com.sangjun.order.domain.event;

import com.sangjun.order.domain.entity.Order;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class OrderCancellingEvent extends ApplicationEvent {

    public OrderCancellingEvent(Object source) {
        super(source);
    }

    public OrderCancellingEvent(Object source, Clock clock) {
        super(source, clock);
    }

    public Order getOrder() {
        return (Order) super.getSource();
    }
}
