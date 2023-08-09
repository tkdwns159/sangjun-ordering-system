package com.sangjun.restaurant.application.ports.input.service;

import com.sangjun.restaurant.application.OrderApprovalEventShooter;
import com.sangjun.restaurant.application.exception.PendingOrderNotFound;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.event.OrderApprovedEvent;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantApprovalApplicationService {
    private final OrderApprovalEventShooter orderApprovalEventShooter;
    private final PendingOrderRepository pendingOrderRepository;

    @Transactional
    public void approveOrder(String pendingOrderId) {
        var pendingOrder = findPendingOrder(pendingOrderId);
        OrderApprovedEvent domainEvent = pendingOrder.approve();
        orderApprovalEventShooter.fire(domainEvent);
    }

    private PendingOrder findPendingOrder(String pendingOrderId) {
        return pendingOrderRepository.findById(toPengOrderId(pendingOrderId))
                .orElseThrow(() -> new PendingOrderNotFound(pendingOrderId));
    }

    private PendingOrderId toPengOrderId(String pendingOrderId) {
        return new PendingOrderId(UUID.fromString(pendingOrderId));
    }
}
