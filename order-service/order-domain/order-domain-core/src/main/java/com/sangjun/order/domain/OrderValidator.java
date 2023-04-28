package com.sangjun.order.domain;

import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.OrderItem;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.exception.OrderDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderValidator {
    private static final Logger log = LoggerFactory.getLogger(OrderValidator.class.getName());

    public static void validate(Order order, Restaurant restaurant) {
        checkRestaurantIsActive(restaurant);
        checkOrderItemsAreValid(order.getItems(), restaurant);
        order.validateOrder();
    }

    private static void checkRestaurantIsActive(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            log.error("Restaurant with id: {} is currently not active", restaurant.getId().getValue());
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() + " is currently not active");
        }
    }

    private static void checkOrderItemsAreValid(List<OrderItem> items, Restaurant restaurant) {
        List<Product> restaurantProducts = restaurant.getProducts();

        Map<ProductId, Product> restaurantProductMap = new HashMap<>(restaurantProducts.size());
        for (Product product : restaurantProducts) {
            restaurantProductMap.put(product.getId(), product);
        }

        for (OrderItem item : items) {
            checkOrderItemIsFromRestaurant(item, restaurantProductMap, restaurant.getId().getValue());
            checkOrderItemPriceEqualsRestaurantProductPrice(item, restaurantProductMap.get(item.getProduct().getId()));
        }
    }

    private static void checkOrderItemPriceEqualsRestaurantProductPrice(OrderItem item, Product product) {
        if (!item.getPrice().equals(product.getPrice())) {
            log.error("OrderItem price {} does not match the restaurant product price", item.getPrice().getAmount());
            throw new OrderDomainException("OrderItem price: " + item.getPrice().getAmount()
                    + " does not match the restaurant product price");
        }
    }

    private static void checkOrderItemIsFromRestaurant(OrderItem item, Map<ProductId, Product> map, UUID restaurantId) {
        if (!map.containsKey(item.getProduct().getId())) {
            log.error("OrderItem product with id: {} is not from restaurant with id: {}",
                    item.getProduct().getId().getValue(), restaurantId);

            throw new OrderDomainException("OrderItem product with id: " + item.getProduct().getId().getValue()
                    + " is not from restaurant with id: " + restaurantId);
        }
    }
}
