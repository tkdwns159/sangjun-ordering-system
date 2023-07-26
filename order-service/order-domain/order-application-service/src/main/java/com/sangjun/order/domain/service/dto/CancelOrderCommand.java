package com.sangjun.order.domain.service.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public
class CancelOrderCommand {
    private UUID customerId;
    private UUID orderTrackingId;
}
