package com.sangjun.restaurant.application.exception;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;

public class PendingOrderNotFound extends RuntimeException {
    public PendingOrderNotFound(PendingOrderId pendingOrderId) {
        super(String.format("pending-order(%s) is not found", pendingOrderId.toString()));
    }

    public PendingOrderNotFound(OrderId orderId) {
        super(String.format("pending-order(orderId: %s) is not found", orderId.toString()));
    }
}
