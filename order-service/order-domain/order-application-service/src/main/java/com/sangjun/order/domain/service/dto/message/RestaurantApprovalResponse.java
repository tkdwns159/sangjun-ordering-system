package com.sangjun.order.domain.service.dto.message;

import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestaurantApprovalResponse {
    private String id;
    private String sagaId;
    private String orderId;
    private String restaurantId;
    private Instant createdAt;
    private OrderApprovalStatus orderApprovalStatus;
    private List<String> failureMessages;
}
