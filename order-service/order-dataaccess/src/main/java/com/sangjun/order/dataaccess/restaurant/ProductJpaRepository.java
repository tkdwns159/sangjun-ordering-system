package com.sangjun.order.dataaccess.restaurant;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.service.ports.output.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ProductJpaRepository implements ProductRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Product> findProductsByRestaurantIdInProductIds(RestaurantId restaurantId, List<ProductId> productIds) {
        List<UUID> ids = productIds.stream()
                .map(ProductId::getValue)
                .toList();

        final String query = "SELECT id, price, quantity FROM restaurant.product WHERE restaurant_id=:rid AND id IN (:ids)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        params.addValue("rid", restaurantId.getValue());

        return jdbcTemplate.queryForList(query, params)
                .stream()
                .map(this::toProduct)
                .collect(Collectors.toList());
    }

    private Product toProduct(Map<String, Object> m) {
        return Product.builder()
                .id(new ProductId((UUID) m.get("id")))
                .price(Money.of((BigDecimal) m.get("price")))
                .quantity((int) m.get("quantity"))
                .build();
    }
}
