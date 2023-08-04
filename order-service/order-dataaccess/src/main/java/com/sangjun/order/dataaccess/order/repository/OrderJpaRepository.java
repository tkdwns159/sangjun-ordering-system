package com.sangjun.order.dataaccess.order.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface OrderJpaRepository extends OrderRepository, Repository<Order, OrderId> {
    @Override
    Optional<Order> findByTrackingId(TrackingId trackingId);

    @Override
    Order save(Order order);

    @Override
    Optional<Order> findById(OrderId orderId);

    @Override
    @Query("select order.orderStatus from Order order where order.trackingId = ?1")
    Optional<OrderStatus> findOrderStatusByTrackingId(TrackingId trackingId);
}
