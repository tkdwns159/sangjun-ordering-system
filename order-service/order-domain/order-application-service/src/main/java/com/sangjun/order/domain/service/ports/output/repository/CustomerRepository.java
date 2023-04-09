package com.sangjun.order.domain.service.ports.output.repository;

import com.sangjun.order.domain.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Optional<Customer> findCustomer(UUID customerId);
}
