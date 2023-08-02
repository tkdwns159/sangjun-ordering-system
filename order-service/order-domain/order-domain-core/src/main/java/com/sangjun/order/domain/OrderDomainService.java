package com.sangjun.order.domain;

import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;

import java.util.List;

public interface OrderDomainService {
    void validateOrder(Order order, Restaurant restaurant);

    OrderCreatedEvent initiateOrder(Order order);

    OrderPaidEvent payOrder(Order order);

    void approveOrder(Order order);

    OrderCancellingEvent initiateOrderCancel(Order order, List<String> failureMessages);

    void cancelOrder(Order order, List<String> failureMessages);
}
