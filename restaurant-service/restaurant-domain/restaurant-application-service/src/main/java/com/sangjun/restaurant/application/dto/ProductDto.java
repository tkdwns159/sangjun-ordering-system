package com.sangjun.restaurant.application.dto;

import com.sangjun.common.domain.valueobject.ProductId;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductDto {
    private ProductId productId;
    private int quantity;
}
