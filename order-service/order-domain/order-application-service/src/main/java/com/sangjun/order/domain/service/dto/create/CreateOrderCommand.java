package com.sangjun.order.domain.service.dto.create;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateOrderCommand {
    @NotNull
    private UUID customerId;

    @NotNull
    private UUID restaurantId;

    @NotNull
    private BigDecimal price;

    @NotNull
    private List<OrderItem> items;

    @NotNull
    private OrderAddress orderAddress;
}
