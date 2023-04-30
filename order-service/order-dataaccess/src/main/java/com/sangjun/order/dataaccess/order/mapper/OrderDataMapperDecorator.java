package com.sangjun.order.dataaccess.order.mapper;


import com.sangjun.order.dataaccess.order.entity.OrderEntity;
import com.sangjun.order.domain.entity.Order;

public abstract class OrderDataMapperDecorator implements OrderDataMapstructMapper {

    private final OrderDataMapstructMapper delegate;

    public OrderDataMapperDecorator(OrderDataMapstructMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public OrderEntity toOrderEntity(Order order) {
        OrderEntity orderEntity = delegate.toOrderEntity(order);
        orderEntity.getItems()
                .forEach(item -> item.setOrder(orderEntity));

        return orderEntity;
    }
}
