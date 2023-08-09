package com.sangjun.restaurant.domain.event;

import com.sangjun.restaurant.domain.entity.PendingOrder;

import java.time.ZonedDateTime;

public class OrderApprovedEvent extends OrderApprovalEvent {

    public OrderApprovedEvent(PendingOrder pendingOrder, ZonedDateTime createdAt) {
        super(pendingOrder, createdAt);
    }
}
