package com.sangjun.order.dataaccess.order.repository;

import com.sangjun.order.dataaccess.order.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository {
    Optional<OrderEntity> findByTrackingId(UUID trackingId);
}
