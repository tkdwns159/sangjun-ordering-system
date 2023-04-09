package com.sangjun.order.domain.service.dto.message;

import com.sangjun.common.domain.valueobject.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentResponse {
    private String id;
    private String sagaId;
    private String orderId;
    private String paymentId;
    private String customerId;
    private BigDecimal price;
    private Instant createdAt;
    private PaymentStatus paymentStatus;
    private List<String> failureMessages;
}
