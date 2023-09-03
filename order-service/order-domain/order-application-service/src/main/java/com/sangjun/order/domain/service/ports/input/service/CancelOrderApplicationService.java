package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.OrderEventShooter;
import com.sangjun.order.domain.service.dto.CancelOrderCommand;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

@Service
@RequiredArgsConstructor
public class CancelOrderApplicationService {
    private final OrderRepository orderRepository;
    private final OrderEventShooter orderEventShooter;

    @Transactional
    public void cancelOrder(CancelOrderCommand command) {
        var trackingId = new TrackingId(command.getOrderTrackingId());
        final Order order = getOrder(trackingId);
        final OrderStatus orderStatus = order.getOrderStatus();

        if (!isNonCancellable(orderStatus)) {
            var orderCancellingEvent = order.initCancel();
            fireDomainEvent(order, orderStatus, orderCancellingEvent);
        }
    }

    private Order getOrder(TrackingId trackingId) {
        return orderRepository.findByTrackingId(trackingId)
                .orElseThrow(() -> new OrderNotFoundException(trackingId));
    }

    private static boolean isNonCancellable(OrderStatus orderStatus) {
        return orderStatus == OrderStatus.CANCELLED
                || orderStatus == OrderStatus.CANCELLING
                || orderStatus == OrderStatus.APPROVED;
    }

    private void fireDomainEvent(Order order,
                                 OrderStatus orderStatus,
                                 OrderCancellingEvent orderCancellingEvent) {
        if (orderStatus == OrderStatus.PENDING) {
            orderEventShooter.fire(orderCancellingEvent);
        } else if (orderStatus == OrderStatus.PAID) {
            var orderPaidEvent = new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
            orderEventShooter.fire(orderPaidEvent);
        }
    }
}
