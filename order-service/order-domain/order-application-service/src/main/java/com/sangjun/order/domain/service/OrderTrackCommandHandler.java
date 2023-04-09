package com.sangjun.order.domain.service;


import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.exception.OrderDomainException;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.mapper.OrderDataMapper;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
public class OrderTrackCommandHandler {

    private final OrderDataMapper orderDataMapper;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderTrackCommandHandler(OrderDataMapper orderDataMapper, OrderRepository orderRepository) {
        this.orderDataMapper = orderDataMapper;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        Optional<Order> foundOrder = orderRepository.findByTrackingId(trackOrderQuery.getOrderTrackingId());
        foundOrder.orElseThrow(()-> {
            log.warn("Could not find order with tracking id : {}", trackOrderQuery.getOrderTrackingId());
            throw new OrderNotFoundException("Could not find order with tracking id" + trackOrderQuery.getOrderTrackingId());}
        );

        return orderDataMapper.orderToTrackOrderResponse(foundOrder.get());
    }
}
