package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.valueobject.TrackingId;

import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByTrackingId(TrackingId trackingId);

    Optional<Order> findById(OrderId orderId);

    Optional<OrderStatus> findOrderStatusByTrackingId(TrackingId trackingId);
    
    Optional<Order> findByIdWithOrderItems(OrderId orderId);
}
