package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.domain.entity.PendingOrder;

import java.util.Optional;

public interface PendingOrderRepository {
    Optional<PendingOrder> findByOrderId(OrderId orderId);

    PendingOrder save(PendingOrder pendingOrder);
}
