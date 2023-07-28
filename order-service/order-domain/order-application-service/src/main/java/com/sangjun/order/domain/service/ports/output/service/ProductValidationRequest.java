package com.sangjun.order.domain.service.ports.output.service;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;

public class ProductValidationRequest {
    private ProductId productId;
    private Money price;
    private int quantity;
}
