package com.sangjun.order.domain.service.dto.create;

import com.sangjun.common.domain.valueobject.OrderStatus;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateOrderResponse {
    @NotNull
    private UUID orderTrackingId;

    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private String message;
}
