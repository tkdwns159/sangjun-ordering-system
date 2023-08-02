package com.sangjun.order.domain.service;

import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.service.dto.CancelOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.dto.track.TrackOrderQuery;
import com.sangjun.order.domain.service.dto.track.TrackOrderResponse;
import com.sangjun.order.domain.service.mapper.OrderMapstructMapper;
import com.sangjun.order.domain.service.ports.input.service.OrderApplicationService;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import com.sangjun.order.domain.service.ports.output.repository.OrderRepository;
import com.sangjun.order.domain.service.ports.output.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
class OrderApplicationServiceImpl implements OrderApplicationService {

    private final CustomerRepository customerRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderDomainService orderDomainService;
    private final OrderRepository orderRepository;
    private final OrderEventShooter orderEventShooter;

    @Override
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        Order order = OrderMapstructMapper.MAPPER.toOrder(createOrderCommand);
        order.initialize();

        return null;
    }

    public TrackOrderResponse trackOrder(TrackOrderQuery trackOrderQuery) {
        return null;
    }

    @Override
    public void cancelOrder(CancelOrderCommand command) {
    }
}
