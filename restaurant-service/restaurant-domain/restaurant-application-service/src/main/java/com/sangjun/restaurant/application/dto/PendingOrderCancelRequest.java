package com.sangjun.restaurant.application.dto;

import com.sangjun.common.domain.valueobject.OrderId;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PendingOrderCancelRequest {
    private OrderId orderId;

    public OrderId getOrderId() {
        return this.orderId;
    }
}
