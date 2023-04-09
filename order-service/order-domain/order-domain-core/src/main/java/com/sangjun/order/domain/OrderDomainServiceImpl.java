package com.sangjun.order.domain;

import com.sangjun.common.domain.event.publisher.DomainEventPublisher;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Product;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCancelledEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Slf4j
public class OrderDomainServiceImpl implements OrderDomainService {

    private static final String ZONE_ID = ZoneId.SHORT_IDS.get("JST");

    @Override
    public Order validateOrder(Order order, Restaurant restaurant) {
        setOrderProductInformation(order, restaurant);
        validateRestaurant(restaurant);
        order.validateOrder();

        return order;
    }

    @Override
    public OrderCreatedEvent initiateOrder(Order order, DomainEventPublisher<OrderCreatedEvent> orderCreatedEventDomainEventPublisher) {
        order.initializeOrder();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(
                order,
                ZonedDateTime.now(ZoneId.of(ZONE_ID)),
                orderCreatedEventDomainEventPublisher);
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() + " is currently not active!");
        }
    }

    private void setOrderProductInformation(Order order, Restaurant restaurant) {
        Map<Product, Integer> restaurantProductsSet = new HashMap<>();

        List<Product> restaurantProducts = restaurant.getProducts();

        for (int i = 0; i < restaurantProducts.size(); i++) {
            restaurantProductsSet.put(restaurantProducts.get(i), i);
        }

        order.getItems().forEach(orderItem -> {
            Product currentProduct = orderItem.getProduct();
            Integer searchResult = restaurantProductsSet.getOrDefault(currentProduct, -1);

            if (searchResult > -1) {
                Product restaurantProduct = restaurantProducts.get(searchResult);
                currentProduct.updateWithConfirmedNameAndPrice(restaurantProduct.getName(), restaurantProduct.getPrice());
            }
        });
    }


    @Override
    public OrderPaidEvent payOrder(Order order, DomainEventPublisher<OrderPaidEvent> orderPaidEventDomainEventPublisher) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)), orderPaidEventDomainEventPublisher);
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancelledEvent cancelOrderPayment(Order order, List<String> failureMessages, DomainEventPublisher<OrderCancelledEvent> orderCancelledEventDomainEventPublisher) {
        order.initCancel(failureMessages);
        log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
        return new OrderCancelledEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)), orderCancelledEventDomainEventPublisher);
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order id: {} has been cancelled", order.getId().getValue());
    }


}