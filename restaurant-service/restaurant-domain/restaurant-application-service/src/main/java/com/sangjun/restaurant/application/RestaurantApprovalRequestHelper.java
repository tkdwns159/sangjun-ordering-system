package com.sangjun.restaurant.application;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.ProductId;
import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.mapper.RestaurantDataMapper;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderApprovedMessagePublisher;
import com.sangjun.restaurant.application.ports.output.message.publisher.OrderRejectedMessagedPublisher;
import com.sangjun.restaurant.application.ports.output.message.repository.OrderApprovalRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.domain.RestaurantDomainService;
import com.sangjun.restaurant.domain.entity.OrderDetail;
import com.sangjun.restaurant.domain.entity.Product;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;
import com.sangjun.restaurant.domain.exception.RestaurantNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class RestaurantApprovalRequestHelper {
    private final RestaurantDataMapper restaurantDataMapper;
    private final RestaurantDomainService restaurantDomainService;
    private final RestaurantRepository restaurantRepository;
    private final OrderApprovalRepository orderApprovalRepository;
    private final OrderApprovedMessagePublisher orderApprovedMessagePublisher;
    private final OrderRejectedMessagedPublisher orderRejectedMessagedPublisher;

    @Transactional
    public OrderApprovalEvent persistOrderApproval(RestaurantApprovalRequest restaurantApprovalRequest) {
        log.info("Processing restaurant approval for order id : {}", restaurantApprovalRequest.getOrderId());
        List<String> failureMessages = new ArrayList<>();
        Restaurant restaurant = findRestaurant(restaurantApprovalRequest);
        OrderApprovalEvent orderApprovalEvent = restaurantDomainService.validateOrder(
                restaurant,
                failureMessages,
                orderApprovedMessagePublisher,
                orderRejectedMessagedPublisher);

        orderApprovalRepository.save(restaurant.getOrderApproval());
        return orderApprovalEvent;
    }

    private Restaurant findRestaurant(RestaurantApprovalRequest restaurantApprovalRequest) {
        Restaurant restaurant = restaurantDataMapper.restaurantApprovalRequestToRestaurant(restaurantApprovalRequest);
        Restaurant foundRestaurant = restaurantRepository.findRestaurantInformation(restaurant)
                .orElseThrow(() -> {
                    log.error("Restaurant with id: {} is not found", restaurant.getId().getValue());
                    return new RestaurantNotFoundException("Restaurant with id: " + restaurant.getId().getValue() + " is not found");
                });

        for (Product product : restaurant.getOrderDetail().getProducts()) {
            for (Product foundProduct : foundRestaurant.getOrderDetail().getProducts()) {
                if (product.getId().equals(foundProduct.getId())) {
                    product.setName(foundProduct.getName());
                    product.setPrice(foundProduct.getPrice());
                    product.setAvailable(foundProduct.isAvailable());
                }
            }
        }
        restaurant.setActive(foundRestaurant.isActive());

        return restaurant;
    }
}
