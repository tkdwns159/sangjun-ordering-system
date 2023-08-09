package com.sangjun.restaurant.domain.event;

import com.sangjun.restaurant.domain.entity.PendingOrder;

import java.time.ZonedDateTime;

public class OrderRejectedEvent extends OrderApprovalEvent {

    public OrderRejectedEvent(PendingOrder pendingOrder, ZonedDateTime createdAt) {
        super(pendingOrder, createdAt);
    }
}
