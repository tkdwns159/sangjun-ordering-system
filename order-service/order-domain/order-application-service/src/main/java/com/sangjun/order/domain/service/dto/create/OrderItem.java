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
    @NotNull
    private UUID productId;
    @NotNull
    private Integer quantity;
    @NotNull
    private BigDecimal price;
    @NotNull
    private BigDecimal subTotal;

}
