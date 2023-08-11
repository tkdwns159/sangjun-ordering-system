package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;

import java.util.Optional;

public interface PendingOrderRepository {
    Optional<PendingOrder> findByOrderId(OrderId orderId);

    PendingOrder save(PendingOrder pendingOrder);

    Optional<PendingOrder> findById(PendingOrderId pendingOrderId);
}
