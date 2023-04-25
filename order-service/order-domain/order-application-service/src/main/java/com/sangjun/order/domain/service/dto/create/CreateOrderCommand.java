package com.sangjun.order.domain.service.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
