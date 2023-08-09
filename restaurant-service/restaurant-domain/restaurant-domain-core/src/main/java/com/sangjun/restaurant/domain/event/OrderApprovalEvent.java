package com.sangjun.restaurant.domain.event;

import com.sangjun.common.domain.event.DomainEvent;
import com.sangjun.restaurant.domain.entity.OrderApproval;
import com.sangjun.restaurant.domain.entity.PendingOrder;

import java.time.ZonedDateTime;


public abstract class OrderApprovalEvent implements DomainEvent<OrderApproval> {
    private final PendingOrder pendingOrder;
    private final ZonedDateTime createdAt;

    public OrderApprovalEvent(PendingOrder pendingOrder, ZonedDateTime createdAt) {
        this.pendingOrder = pendingOrder;
        this.createdAt = createdAt;
    }

    public PendingOrder getPendingOrder() {
        return pendingOrder;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
}
