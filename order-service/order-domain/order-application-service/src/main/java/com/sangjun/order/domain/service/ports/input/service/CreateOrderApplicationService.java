package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.service.OrderEventShooter;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.create.OrderItemDto;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.service.customer.CustomerCheckService;
import com.sangjun.order.domain.service.ports.output.service.product.ProductValidationService;
import com.sangjun.order.domain.valueobject.Product;
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
        checkCustomerExistence(new CustomerId(command.getCustomerId()));
        validateProducts(command.getItems());

        final Order order = MAPPER.toOrder(command);
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

    private void validateProducts(List<OrderItemDto> items) {
        var products = toProducts(items);
        productValidationService.validateProducts(products);
    }

    private List<Product> toProducts(List<OrderItemDto> items) {
        return items.stream()
                .map(MAPPER::toProduct)
                .toList();
    }
}
