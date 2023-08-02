package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.valueobject.Product;

import java.util.List;

public interface RestaurantRepository {

    List<Product> findProductsByRestaurantIdInProductIds(RestaurantId restaurantId, List<ProductId> productIds);

}
