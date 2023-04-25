package com.sangjun.order.domain.service;

import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
class OrderApplicationServiceImpl implements OrderApplicationService {

    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        Order orderDraft = MAPPER.toOrder(createOrderCommand);
        validateCommand(orderDraft);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.initiateOrder(orderDraft);
        Order order = orderCreatedEvent.getOrder();

        return MAPPER.toCreateOrderResponse(order);
    }

    private void validateCommand(Order orderDraft) {
        checkCustomerIsValid(orderDraft.getCustomerId().getValue());
        Restaurant restaurant = getRestaurant(orderDraft);
        checkRestaurantIsActive(restaurant);
        orderDomainService.validateOrder(orderDraft, restaurant);
    }

    private void checkCustomerIsValid(UUID customerId) {
        customerRepository
                .findCustomer(customerId)
                .orElseThrow(() -> {
                    log.error("Customer: {} not found", customerId);
                    return new OrderDomainException("Customer: " + customerId
                            + " not found");
                });
    }

    private Restaurant getRestaurant(Order orderDraft) {
        return restaurantRepository
                .findRestaurantInformation(MAPPER.toRestaurant(orderDraft))
                .orElseThrow(() -> {
                    log.error("Restaurant with id: {} not found", orderDraft.getRestaurantId().getValue());
                    return new OrderDomainException("Restaurant with id: " + orderDraft.getRestaurantId().getValue()
                            + " not found");
                });
    }

    private static void checkRestaurantIsActive(Restaurant restaurant) {
        if (!restaurant.isActive()) {
            log.error("Restaurant with id: {} is currently not active", restaurant.getId().getValue());
            throw new OrderDomainException("Restaurant with id " + restaurant.getId().getValue() + " is currently not active");
        }
    }

    @Override
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Order order = getOrderByTrackingId(trackOrderQuery.getOrderTrackingId());

        return MAPPER.toTrackOrderResponse(order);
    }

    private Order getOrderByTrackingId(UUID trackingId) {
        return orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> {
                    log.error("Order with tracking id: {} not found", trackingId);
                    return new OrderDomainException("Order with tracking id " + trackingId + " not found");
                });
    }
}
