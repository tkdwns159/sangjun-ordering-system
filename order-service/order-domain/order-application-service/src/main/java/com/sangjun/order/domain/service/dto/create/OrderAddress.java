package com.sangjun.order.domain.service.dto.create;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderAddress {
    @NotNull(message = "street is required")
    @Max(value = 50)
    private String street;

    @NotNull(message = "postalCode is required")
    @Max(value = 10)
    private String postalCode;

    @NotNull(message = "city is required")
    @Max(value = 50)
    private String city;
}
