package com.sangjun.order.domain.service;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.OrderStatus;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.valueobject.StreetAddress;
import com.sangjun.order.domain.valueobject.TrackingId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderTrackTest {
    @InjectMocks
    private OrderApplicationServiceImpl orderApplicationService;
    @Mock
    private OrderRepository orderRepository;

    @Test
    void 주문확인이_성공한다() {
        UUID trackingId = UUID.randomUUID();
        TrackOrderQuery trackOrderQuery = new TrackOrderQuery(trackingId);
        Order order = Order.builder()
                .orderId(new OrderId(UUID.randomUUID()))
                .trackingId(new TrackingId(trackingId))
                .orderStatus(OrderStatus.PENDING)
                .customerId(new CustomerId(UUID.randomUUID()))
                .items(new ArrayList<>())
                .price(Money.ZERO)
                .deliveryAddress(StreetAddress.builder()
                        .id(UUID.randomUUID())
                        .street("Silim")
                        .city("Seoul")
                        .postalCode("14123")
                        .build())
                .failureMessages(new ArrayList<>())
                .build();

        when(orderRepository.findByTrackingId(trackingId))
                .thenReturn(Optional.of(order));

        TrackOrderResponse response = orderApplicationService.trackOrder(trackOrderQuery);
        assertEquals(trackingId, response.getOrderTrackingId());
        assertNotNull(response.getOrderStatus());
    }
}
