package com.sangjun.order.domain.exception;

import com.sangjun.common.domain.exception.DomainException;
import com.sangjun.order.domain.valueobject.TrackingId;

public class OrderNotFoundException extends DomainException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(TrackingId trackingId) {
        super(String.format("Order not found with trackingId: %s", trackingId.getValue()));
    }

}
