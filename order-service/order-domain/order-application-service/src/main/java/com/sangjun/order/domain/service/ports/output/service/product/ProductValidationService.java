package com.sangjun.order.domain.service.ports.output.service.product;

import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.entity.Product;

import java.util.List;

public interface ProductValidationService {
    void validateProducts(RestaurantId restaurantId, List<Product> products);

}
