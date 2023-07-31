package com.sangjun.order.domain.service.ports.output.service.product;

import com.sangjun.order.domain.valueobject.Product;

import java.util.List;

public interface ProductValidationService {
    void validateProducts(List<Product> products);

}
