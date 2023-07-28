package com.sangjun.order.domain.service.ports.output.service;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductValidationResponse {
    private boolean isSuccessful;
    private String errorMsg;
}
