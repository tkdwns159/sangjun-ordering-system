package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import org.springframework.stereotype.Service;

@Service
public class CreateOrderApplicationService {

    public CreateOrderResponse createOrder(CreateOrderCommand command) {
        return null;
    }
}
