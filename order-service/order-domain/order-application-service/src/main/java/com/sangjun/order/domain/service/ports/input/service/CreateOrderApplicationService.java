package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.service.ProductValidationRequest;
import com.sangjun.order.domain.service.ports.output.service.ProductValidationResponse;
import com.sangjun.order.domain.service.ports.output.service.RestaurantService;
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
    private final RestaurantService restaurantService;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        final Order order = MAPPER.toOrder(command);
        validateProducts(order);
        order.initialize();
        final Order savedOrder = orderRepository.save(order);

        return MAPPER.toCreateOrderResponse(savedOrder);
    }

    private void validateProducts(Order order) {
        var productValidationRequests = toProductValidationRequestList(order.getOrderItems());
        ProductValidationResponse response = restaurantService.validateProducts(productValidationRequests);
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
