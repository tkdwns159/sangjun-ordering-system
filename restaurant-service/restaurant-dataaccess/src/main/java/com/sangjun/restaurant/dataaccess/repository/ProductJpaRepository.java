package com.sangjun.restaurant.dataaccess.repository;

import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.restaurant.application.ports.output.message.repository.ProductRepository;
import com.sangjun.restaurant.domain.entity.Product;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ProductJpaRepository extends ProductRepository, Repository<Product, ProductId> {
    @Override
    List<Product> findAllByRestaurantIdAndIdIn(RestaurantId restaurantId, List<ProductId> productIds);
}
