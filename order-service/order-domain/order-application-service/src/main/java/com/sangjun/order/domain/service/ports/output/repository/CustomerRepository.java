package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.entity.Customer;

import java.util.Optional;

public interface CustomerRepository {

    Optional<Customer> findById(CustomerId customerId);
}
