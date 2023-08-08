package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.restaurant.domain.entity.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAllByIdIn(List<ProductId> productIds);
}
