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
        return orderRepository
                .findById(new OrderId(UUID.fromString(orderId)))
                .orElseThrow(() -> {
                    log.error("Order with id: {} could not be found", orderId);
                    throw new OrderNotFoundException("Order with id: " + orderId + " could not be found");
                });
    }

    public void loadOrderItems(Order order) {
        order.getItems().forEach(OrderItem::getPrice);
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
