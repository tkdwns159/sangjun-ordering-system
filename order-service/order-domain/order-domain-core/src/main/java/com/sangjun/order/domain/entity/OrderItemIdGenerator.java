package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.EmbeddedIdGenerator;
import com.sangjun.order.domain.valueobject.OrderItemId;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.io.Serializable;

public class OrderItemIdGenerator extends EmbeddedIdGenerator {
    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException {
        return new OrderItemId((Long) super.generate(session, obj));
    }
}
