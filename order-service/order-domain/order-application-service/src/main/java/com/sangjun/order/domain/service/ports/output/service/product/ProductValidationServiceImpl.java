package com.sangjun.order.domain.service.ports.output.service.product;

import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import com.sangjun.order.domain.valueobject.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductValidationServiceImpl implements ProductValidationService {
    private final RestaurantRepository restaurantRepository;

    @Override
    public void validateProducts(RestaurantId restaurantId, List<Product> products) {
        List<Product> foundProducts = restaurantRepository
                .findProductsByRestaurantIdInProductIds(restaurantId, getProductIds(products));

        Map<ProductId, Product> foundProductMap = toHashMap(foundProducts);

        for (var product : products) {
            Product rProduct = foundProductMap.get(product.getId());

            if (rProduct == null) {
                throw new IllegalArgumentException(
                        String.format("product(%s) not found in restaurant(%s)",
                                product.getId().getValue(), restaurantId.getValue()));
            }

            if (!rProduct.getPrice().equals(product.getPrice())) {
                throw new IllegalArgumentException(
                        String.format("requested product(%s) price(%s) is different from the original product price(%s)",
                                product.getId().getValue(), product.getPrice(), rProduct.getPrice()));
            }

            if (!rProduct.getName().equalsIgnoreCase(product.getName())) {
                throw new IllegalArgumentException(
                        String.format("original product(%s) name is %s, but %s",
                                rProduct.getId().getValue(), rProduct.getName(), product.getName()));
            }


            if (rProduct.getQuantity() < product.getQuantity()) {
                throw new IllegalArgumentException(
                        String.format("product stock(%d) is lower than requested quantity(%d)",
                                rProduct.getQuantity(), product.getQuantity()));
            }
        }
    }

    private List<ProductId> getProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    private Map<ProductId, Product> toHashMap(List<Product> foundProducts) {
        return foundProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
