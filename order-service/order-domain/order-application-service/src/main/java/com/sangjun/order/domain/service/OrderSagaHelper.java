package com.sangjun.order.domain.service;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaHelper {
    private final OrderRepository orderRepository;

    public Order findOrder(String orderId) {
        OrderId id = new OrderId(UUID.fromString(orderId));
        return orderRepository
                .findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public void loadOrderItems(Order order) {
        order.getItems().forEach(OrderItem::getPrice);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
