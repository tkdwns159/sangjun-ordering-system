package com.sangjun.order.domain.entity;

import com.sangjun.common.domain.valueobject.CustomerId;

public class Customer {

    private CustomerId customerId;

    public Customer(CustomerId customerId) {
        this.customerId = customerId;
    }
}
