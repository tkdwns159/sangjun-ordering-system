package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sangjun.order.domain.service.mapper.OrderMapstructMapper.MAPPER;

@Service
@RequiredArgsConstructor
public class CreateOrderApplicationService {
    private final OrderRepository orderRepository;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        final Order order = MAPPER.toOrder(command);
        order.initialize();
        final Order savedOrder = orderRepository.save(order);

        return MAPPER.toCreateOrderResponse(savedOrder);
    }
}
