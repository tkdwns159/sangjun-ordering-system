package com.sangjun.order.domain.service.ports.output.service;

import java.util.List;

public interface RestaurantService {
    ProductValidationResponse validateProducts(List<ProductValidationRequest> requests);

}
