package com.sangjun.order.domain.service;

import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Validated
@Service
class OrderApplicationServiceImpl implements OrderApplicationService {
    private final OrderCreateCommandHandler orderCreateCommandHandler;
    private final OrderTrackCommandHandler orderTrackCommandHandler;

    @Autowired
    public OrderApplicationServiceImpl(OrderCreateCommandHandler orderCreateCommandHandler, OrderTrackCommandHandler orderTrackCommandHandler) {
        this.orderCreateCommandHandler = orderCreateCommandHandler;
        this.orderTrackCommandHandler = orderTrackCommandHandler;
    }

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        return orderCreateCommandHandler.createOrder(createOrderCommand);
    }

    @Override
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return orderTrackCommandHandler.trackOrder(trackOrderQuery);
    }
}
