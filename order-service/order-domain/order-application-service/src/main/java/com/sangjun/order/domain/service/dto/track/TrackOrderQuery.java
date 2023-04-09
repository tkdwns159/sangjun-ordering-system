package com.sangjun.order.domain.service.dto.track;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrackOrderQuery {
    @NotNull
    private UUID orderTrackingId;
}
