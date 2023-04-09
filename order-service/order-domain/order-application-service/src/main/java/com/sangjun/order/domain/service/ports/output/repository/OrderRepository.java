package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.order.domain.entity.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByTrackingId(UUID trackingId);

    Optional<Order> findById(OrderId orderId);
}
