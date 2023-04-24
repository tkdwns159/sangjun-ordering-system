package com.sangjun.order.domain;

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

public class OrderValidator {
    private static final Logger log = LoggerFactory.getLogger(OrderValidator.class.getName());

    public static void validate(Order order, Restaurant restaurant) {
        checkRestaurantIsActive(restaurant);
        checkOrderItemsAreFromRestaurant(order.getItems(), restaurant);
        order.validateOrder();
    }

    private static void checkRestaurantIsActive(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            log.error("Restaurant with id: {} is currently not active", restaurant.getId().getValue());
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() + " is currently not active");
        }
    }

    private static void checkOrderItemsAreFromRestaurant(List<OrderItem> items, Restaurant restaurant) {
        List<Product> restaurantProducts = restaurant.getProducts();

        Map<Product, Integer> restaurantProductsSet = new HashMap<>(restaurantProducts.size());
        for (int i = 0; i < restaurantProducts.size(); i++) {
            restaurantProductsSet.put(restaurantProducts.get(i), i);
        }

        for (OrderItem item : items) {
            if (!restaurantProductsSet.containsKey(item.getProduct())) {
                log.error("OrderItem with id: {} is not from restaurant with id: {}",
                        item.getId().getValue(), restaurant.getId().getValue());

                throw new OrderDomainException("OrderItem with id: " + item.getId().getValue()
                        + " is not from restaurant with id: " + restaurant.getId().getValue());
            }
        }
    }
}
