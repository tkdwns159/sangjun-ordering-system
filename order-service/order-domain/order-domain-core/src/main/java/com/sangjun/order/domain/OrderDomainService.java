package com.sangjun.order.domain;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {
    Order validateOrder(Order order, Restaurant restaurant);

    OrderCreatedEvent initiateOrder(Order order, DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher);

    OrderPaidEvent payOrder(Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher);

    void approveOrder(Order order);

    OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages, DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher);

    void cancelOrder(Order order, List<String> failureMessages);
}
