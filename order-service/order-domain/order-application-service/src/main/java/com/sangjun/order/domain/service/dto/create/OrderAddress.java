package com.sangjun.order.domain.service.dto.create;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderAddress {
    @NotNull
    @Max(value = 50)
    private String street;

    @NotNull
    @Max(value = 10)
    private String postalCode;

    @NotNull
    @Max(value = 50)
    private String city;
}
