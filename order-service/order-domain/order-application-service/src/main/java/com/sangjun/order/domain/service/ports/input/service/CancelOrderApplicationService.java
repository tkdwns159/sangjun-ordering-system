package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.OrderEventShooter;
import com.sangjun.order.domain.service.dto.CancelOrderCommand;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

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
        final Order order = orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new OrderNotFoundException(trackingId));
        final OrderStatus prevOrderStatus = order.getOrderStatus();
        var orderCancellingEvent = orderDomainService.initiateOrderCancel(order, new ArrayList<>());
        
        orderEventShooter.fire(orderCancellingEvent);
        if (prevOrderStatus == OrderStatus.PAID) {
            var orderPaidEvent = new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
            orderEventShooter.fire(orderPaidEvent);
        }
    }

}
