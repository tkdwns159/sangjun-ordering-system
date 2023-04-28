package com.sangjun.order.domain.service.dto.create;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItem {
    @NotNull(message = "Item productId is required")
    private UUID productId;
    @NotNull(message = "Item quantity is required")
    private Integer quantity;
    @NotNull(message = "Item price is required")
    private BigDecimal price;
    @NotNull(message = "Item subTotal is required")
    private BigDecimal subTotal;

}
