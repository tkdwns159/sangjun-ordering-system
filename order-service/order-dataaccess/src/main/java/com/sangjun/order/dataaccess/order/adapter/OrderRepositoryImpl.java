package com.sangjun.order.dataaccess.order.adapter;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.order.dataaccess.order.mapper.OrderDataAccessMapper;
import com.sangjun.order.dataaccess.order.repository.OrderJpaRepository;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderDataAccessMapper orderDataAccessMapper;

    @Override
    public Order save(Order order) {
        return orderDataAccessMapper
                .orderEntityToOrder(
                        orderJpaRepository.save(orderDataAccessMapper.orderToOrderEntity(order))
                );
    }

    @Override
    public Optional<Order> findByTrackingId(UUID trackingId) {
        return orderJpaRepository
                .findByTrackingId(trackingId)
                .map(orderDataAccessMapper::orderEntityToOrder);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue())
                .map(orderDataAccessMapper::orderEntityToOrder);
    }
}
