package com.sangjun.order.domain.service;

import com.sangjun.order.domain.OrderDomainService;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.service.dto.create.CreateOrderCommand;
import com.sangjun.order.domain.service.dto.create.CreateOrderResponse;
import com.sangjun.order.domain.service.mapper.OrderDataMapper;
import com.sangjun.order.domain.service.ports.output.message.publisher.payment.OrderCreatedPaymentRequestMessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreateCommandHandler {

    private final OrderDomainService orderDomainService;
    private final OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher;
    private final OrderCreateHelper orderCreateService;
    private final OrderDataMapper orderDataMapper;

    @Autowired
    public OrderCreateCommandHandler(OrderDomainService orderDomainService, OrderCreatedPaymentRequestMessagePublisher orderCreatedPaymentRequestMessagePublisher, OrderCreateHelper orderCreateService, OrderDataMapper orderDataMapper) {
        this.orderDomainService = orderDomainService;
        this.orderCreatedPaymentRequestMessagePublisher = orderCreatedPaymentRequestMessagePublisher;
        this.orderCreateService = orderCreateService;
        this.orderDataMapper = orderDataMapper;
    }

    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand) {
        OrderCreatedEvent orderCreatedEvent = orderCreateService.createOrder(createOrderCommand);

        orderCreatedPaymentRequestMessagePublisher.publish(orderCreatedEvent);

        return orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(), "Order Created Successfully");
    }

}
