package com.sangjun.restaurant.application;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.application.dto.PendingOrderCancelRequest;
import com.sangjun.restaurant.application.exception.PendingOrderNotFound;
import com.sangjun.restaurant.application.ports.input.message.listener.PendingOrderCancelRequestMessageListener;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PendingOrderCancelRequestMessageListenerImpl implements PendingOrderCancelRequestMessageListener {
    private final PendingOrderRepository pendingOrderRepository;

    @Transactional
    @Override
    public void cancelPendingOrder(PendingOrderCancelRequest pendingOrderCancelRequest) {
        OrderId orderId = pendingOrderCancelRequest.getOrderId();
        PendingOrder pendingOrder = findPendingOrder(orderId);
        if (pendingOrder.getStatus() == PendingOrderStatus.PENDING) {
            pendingOrder.reject();
        }
    }

    private PendingOrder findPendingOrder(OrderId orderId) {
        return pendingOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PendingOrderNotFound(orderId));
    }
}
