package com.sangjun.order.domain;

import com.sangjun.order.domain.entity.Order;
import com.sangjun.order.domain.entity.Restaurant;
import com.sangjun.order.domain.event.OrderCancellingEvent;
import com.sangjun.order.domain.event.OrderCreatedEvent;
import com.sangjun.order.domain.event.OrderPaidEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

public class OrderDomainServiceImpl implements OrderDomainService {

    private static final Logger log = LoggerFactory.getLogger(OrderDomainServiceImpl.class.getName());

    @Override
    public void validateOrder(Order order, Restaurant restaurant) {
    }

    @Override
    public OrderCreatedEvent initiateOrder(Order order) {
        order.initialize();
        log.info("Order with id: {} is initiated", order.getId().getValue());
        return new OrderCreatedEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    @Override
    public OrderPaidEvent payOrder(Order order) {
        order.pay();
        log.info("Order with id: {} is paid", order.getId().getValue());
        return new OrderPaidEvent(order, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
    }

    @Override
    public void approveOrder(Order order) {
        order.approve();
        log.info("Order with id: {} is approved", order.getId().getValue());
    }

    @Override
    public OrderCancellingEvent initiateOrderCancel(Order order, List<String> failureMessages) {
        order.initCancel();
        log.info("Order payment is cancelling for order id: {}", order.getId().getValue());
        return new OrderCancellingEvent(order, Clock.system(ZoneId.of(ZONE_ID)));
    }

    @Override
    public void cancelOrder(Order order, List<String> failureMessages) {
        order.cancel(failureMessages);
        log.info("Order id: {} has been cancelled", order.getId().getValue());
    }
}