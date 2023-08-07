package com.sangjun.restaurant.dataaccess.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface PendingOrderJpaRepository extends PendingOrderRepository, Repository<PendingOrder, PendingOrderId> {
    @Override
    Optional<PendingOrder> findByOrderId(OrderId orderId);

    @Override
    PendingOrder save(PendingOrder pendingOrder);
}
