package com.sangjun.order.domain.service.dto.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderCommand {
    @NotNull(message = "CustomerId is required")
    private UUID customerId;

    @NotNull(message = "RestaurantId is required")
    private UUID restaurantId;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @Valid
    @NotNull(message = "Items is required")
    @Size(min = 1, message = "Must be [items.size() >= 1]")
    private List<OrderItemDto> items;

    @NotNull(message = "OrderAddressDto is required")
    private OrderAddressDto orderAddressDto;
}
