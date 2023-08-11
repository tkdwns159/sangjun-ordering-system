package com.sangjun.restaurant.application.ports.input.message.listener;

import com.sangjun.restaurant.application.dto.PendingOrderCancelRequest;

public interface PendingOrderCancelRequestMessageListener {
    void cancelPendingOrder(PendingOrderCancelRequest pendingOrderCancelRequest);
}
