package com.sangjun.order.dataaccess;

import com.sangjun.common.dataaccess.restaurant.entity.RestaurantEntity;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.order.domain.valueobject.Product;
import com.sangjun.order.domain.valueobject.Restaurant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.sangjun.order.dataaccess.restaurant.mapper.RestaurantDataMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RestaurantDataMapperTest {

    private static final UUID PRODUCT_ID_1 = UUID.randomUUID();
    public static final UUID PRODUCT_ID_2 = UUID.randomUUID();
    public static final UUID RESTAURANT_ID = UUID.randomUUID();
    public static final Product PRODUCT_1 = Product.builder()
            .id(new ProductId(PRODUCT_ID_1))
            .price(Money.of(new BigDecimal("1000")))
            .name("product1")
            .build();
    public static final Product PRODUCT_2 = Product.builder()
            .id(new ProductId(PRODUCT_ID_2))
            .price(Money.of(new BigDecimal("2300")))
            .name("product2")
            .build();

    @Test
    void restaurantToRestaurantProductId() {
        List<Product> products = new ArrayList<>();
        products.add(PRODUCT_1);
        products.add(PRODUCT_2);

        Restaurant restaurant = Restaurant.builder()
                .products(products)
                .build();

        List<UUID> productIds = MAPPER.toRestaurantProductId(restaurant);

        assertEquals(2, productIds.size());
        assertTrue(productIds.contains(PRODUCT_ID_1));
        assertTrue(productIds.contains(PRODUCT_ID_2));
    }

    @Test
    void restaurantEntityToRestaurant() {
        List<RestaurantEntity> entities = new ArrayList<>();
        entities.add(getRestaurantEntity(PRODUCT_1));
        entities.add(getRestaurantEntity(PRODUCT_2));

        Restaurant restaurant = MAPPER.toRestaurant(entities);

        assertEquals(RESTAURANT_ID, restaurant.getId().getValue());
        assertTrue(restaurant.isActive());
        assertEquals(2, restaurant.getProducts().size());
    }

    private static RestaurantEntity getRestaurantEntity(Product product) {
        return RestaurantEntity.builder()
                .restaurantId(RESTAURANT_ID)
                .restaurantActive(true)
                .restaurantName("test")
                .productId(product.getId().getValue())
                .productAvailable(true)
                .productPrice(product.getPrice().getAmount())
                .productName(product.getName())
                .build();
    }

}
