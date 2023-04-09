package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.entity.AggregateRoot;
import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.CustomerId;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
public class Customer extends AggregateRoot<CustomerId> {

    public Customer(CustomerId customerId) {
        super.setId(customerId);
    }
}
