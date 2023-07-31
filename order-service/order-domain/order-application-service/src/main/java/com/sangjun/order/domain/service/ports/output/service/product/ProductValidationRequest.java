package com.sangjun.order.domain.service.ports.output.service.product;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductValidationRequest {
    private final ProductId productId;
    private final Money price;
    private final int quantity;
}
