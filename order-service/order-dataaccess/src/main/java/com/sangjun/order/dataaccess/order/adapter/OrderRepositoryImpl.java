package com.sangjun.order.dataaccess.order.adapter;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.order.dataaccess.order.repository.OrderJpaRepository;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.sangjun.order.dataaccess.order.mapper.OrderDataMapstructMapper.MAPPER;

@Component
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return MAPPER.toOrder(orderJpaRepository.save(MAPPER.toOrderEntity(order)));
    }

    @Override
    public Optional<Order> findByTrackingId(UUID trackingId) {
        return orderJpaRepository
                .findByTrackingId(trackingId)
                .map(MAPPER::toOrder);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return orderJpaRepository.findById(orderId.getValue())
                .map(MAPPER::toOrder);
    }
}
