package com.sangjun.order.domain.service;

import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.mapper.OrderDataMapper;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateHelper {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDataMapper orderDataMapper;
    private final OrderDomainService orderDomainService;
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedEventDomainEventPublisher;

    @Transactional
    public OrderCreatedEvent createOrder(CreateOrderCommand createOrderCommand) {
        Order validOrder = createValidOrder(createOrderCommand);
        OrderCreatedEvent orderCreatedEvent = orderDomainService.initiateOrder(validOrder, orderCreatedEventDomainEventPublisher);

        Order savedOrder = saveOrder(orderCreatedEvent.getOrder());
        log.info("Order created with id : {}", savedOrder.getId());

        return orderCreatedEvent;
    }

    private Order createValidOrder(CreateOrderCommand createOrderCommand) {
        checkCustomer(createOrderCommand.getCustomerId());
        Restaurant restaurant = getRestaurant(createOrderCommand);
        Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
        return orderDomainService.validateOrder(order, restaurant);
    }

    private void checkCustomer(UUID customerId) {
        customerRepository.findCustomer(customerId)
                .orElseThrow(() -> {
                    log.warn("Could not find customer with customer id : {}", customerId);
                    return new OrderDomainException("Could not find customer with customer id : " + customerId);
                });
    }

    private Restaurant getRestaurant(CreateOrderCommand createOrderCommand) {
        Restaurant restaurant = orderDataMapper.createOrderCommandToRestaurant(createOrderCommand);
        Optional<Restaurant> optionalRestaurant = restaurantRepository.findRestaurantInformation(restaurant);

        optionalRestaurant.orElseThrow(() -> {
            log.warn("Could not find restaurant with restaurant id : {}", createOrderCommand.getRestaurantId());
            throw new OrderDomainException("Could not find restaurant with restaurant id : " + createOrderCommand.getRestaurantId());
        });

        return optionalRestaurant.get();
    }

    private Order saveOrder(Order order) {
        Order savedOrder = orderRepository.save(order);

        if (savedOrder == null) {
            log.warn("Could not save order!");
            throw new OrderDomainException("Could not save order!");
        }
        log.info("Order is saved with id : {}", savedOrder.getId().getValue());

        return savedOrder;
    }
}
