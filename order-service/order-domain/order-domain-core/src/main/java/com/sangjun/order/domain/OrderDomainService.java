package com.sangjun.order.domain;

import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.valueobject.Restaurant;

import java.util.List;

public interface OrderDomainService {
    void validateOrder(Order order, Restaurant restaurant);

    OrderCreatedEvent initiateOrder(Order order);

    OrderPaidEvent payOrder(Order order);

    void approveOrder(Order order);

    OrderCancelledEvent initiateOrderCancel(Order order, List<String> failureMessages);

    void cancelOrder(Order order, List<String> failureMessages);
}
