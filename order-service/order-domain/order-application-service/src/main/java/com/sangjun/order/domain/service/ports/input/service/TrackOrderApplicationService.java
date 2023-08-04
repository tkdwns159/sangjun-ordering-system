package com.sangjun.order.domain.service.ports.input.service;

import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.exception.OrderNotFoundException;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.TrackingId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackOrderApplicationService {
    private final OrderRepository orderRepository;

    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        var trackingId = new TrackingId(trackOrderQuery.getOrderTrackingId());
        OrderStatus orderStatus = orderRepository.findOrderStatusByTrackingId(trackingId)
                .orElseThrow(() -> new OrderNotFoundException(trackingId));

        return TrackOrderResponse.builder()
                .orderTrackingId(trackingId.getValue())
                .orderStatus(orderStatus)
                .build();
    }
}
