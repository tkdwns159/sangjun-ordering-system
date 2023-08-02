package com.sangjun.order.domain.service.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class CancelOrderCommand {
    private UUID customerId;
    private UUID orderTrackingId;
}
