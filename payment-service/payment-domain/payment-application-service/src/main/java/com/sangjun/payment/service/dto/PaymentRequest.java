package com.sangjun.payment.service.dto;

import com.sangjun.common.domain.valueobject.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private String id;
    private String orderId;
    private String customerId;
    private String restaurantId;
    private BigDecimal price;
    private Instant createdAt;
    private PaymentStatus paymentStatus;
}
