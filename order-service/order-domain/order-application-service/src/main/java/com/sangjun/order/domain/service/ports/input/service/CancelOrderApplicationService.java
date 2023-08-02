package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.OrderEventShooter;
import com.sangjun.order.domain.service.dto.CancelOrderCommand;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CancelOrderApplicationService {
    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService;
    private final OrderEventShooter orderEventShooter;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        var trackingId = new TrackingId(command.getOrderTrackingId());
        Order order = orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new OrderNotFoundException(trackingId));
        OrderCancellingEvent orderCancellingEvent = orderDomainService.initiateOrderCancel(order, new ArrayList<>());
        eventPublisher.publishEvent(orderCancellingEvent);
    }

}
