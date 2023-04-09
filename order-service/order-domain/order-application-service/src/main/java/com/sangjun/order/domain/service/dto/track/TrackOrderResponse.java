package com.sangjun.order.domain.service.dto.track;


import com.sangjun.common.domain.valueobject.OrderStatus;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackOrderResponse {
    @NotNull
    private UUID orderTrackingId;
    @NotNull
    private OrderStatus orderStatus;
    private List<String> failureMessages;
}
