package com.sangjun.restaurant.application.exception;

public class PendingOrderNotFound extends RuntimeException {
    public PendingOrderNotFound(String pendingOrderId) {
        super(String.format("pending-order(%s) is not found", pendingOrderId));
    }
}
