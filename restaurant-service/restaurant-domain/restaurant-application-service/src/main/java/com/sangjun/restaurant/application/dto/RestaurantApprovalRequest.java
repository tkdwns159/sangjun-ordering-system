package com.sangjun.restaurant.application.dto;

import com.sangjun.common.domain.valueobject.RestaurantOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantApprovalRequest {
    private String restaurantId;
    private String orderId;
    private RestaurantOrderStatus restaurantOrderStatus;
    private List<ProductDto> products;
    private BigDecimal price;
    private Instant createdAt;
}
