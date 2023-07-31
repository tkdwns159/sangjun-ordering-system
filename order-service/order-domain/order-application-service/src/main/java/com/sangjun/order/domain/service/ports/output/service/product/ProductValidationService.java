package com.sangjun.order.domain.service.ports.output.service.product;

import java.util.List;

public interface ProductValidationService {
    ProductValidationResponse validateProducts(List<ProductValidationRequest> requests);

}
