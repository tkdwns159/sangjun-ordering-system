package com.sangjun.restaurant.application;

import com.sangjun.restaurant.application.dto.RestaurantApprovalRequest;
import com.sangjun.restaurant.application.ports.input.message.listener.RestaurantApprovalRequestMessageListener;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.sangjun.restaurant.application.mapper.RestaurantApplicationMapper.MAPPER;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantApprovalRequestMessageListenerImpl implements RestaurantApprovalRequestMessageListener {
    private final OrderApprovalEventShooter orderApprovalEventShooter;
    private final PendingOrderRepository pendingOrderRepository;

    @Override
    public void registerPendingOrder(RestaurantApprovalRequest restaurantApprovalRequest) {
        var pendingOrder = MAPPER.toPendingOrder(restaurantApprovalRequest);
        pendingOrderRepository.save(pendingOrder);
    }
}
