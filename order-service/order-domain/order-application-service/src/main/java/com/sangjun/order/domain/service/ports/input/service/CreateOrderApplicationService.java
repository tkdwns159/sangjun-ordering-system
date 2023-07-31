package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.service.OrderEventShooter;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.service.customer.CustomerCheckService;
import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationRequest;
import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationResponse;
import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationService;
import com.sangjun.order.domain.valueobject.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;

@Service
@RequiredArgsConstructor
public class CreateOrderApplicationService {
    private final OrderRepository orderRepository;
    private final OrderEventShooter orderEventShooter;
    private final ProductValidationService productValidationService;
    private final CustomerCheckService customerCheckService;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        final Order order = MAPPER.toOrder(command);
        checkCustomerExistence(order.getCustomerId());
        validateProducts(order);
        OrderCreatedEvent domainEvent = order.initialize();
        final Order savedOrder = orderRepository.save(domainEvent.getOrder());
        orderEventShooter.fire(domainEvent);
        return MAPPER.toCreateOrderResponse(savedOrder);
    }

    private void checkCustomerExistence(CustomerId customerId) {
        if (!customerCheckService.existsById(customerId)) {
            throw new IllegalStateException(String.format("customer(%s) not found", customerId.getValue()));
        }
    }

    private void validateProducts(Order order) {
        var productValidationRequests = toProductValidationRequestList(order.getItems());
        ProductValidationResponse response = productValidationService.validateProducts(productValidationRequests);
        if (!response.isSuccessful()) {
            throw new IllegalStateException(response.getErrorMsg());
        }
    }

    private List<ProductValidationRequest> toProductValidationRequestList(List<OrderItem> items) {
        return items.stream()
                .map(MAPPER::toProductValidationRequest)
                .toList();
    }
}
