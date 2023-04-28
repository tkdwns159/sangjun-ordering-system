package com.sangjun.order.dataaccess.customer.adapter;

import com.sangjun.order.dataaccess.customer.mapper.CustomerDataAccessMapper;
import com.sangjun.order.dataaccess.customer.repository.CustomerJpaRepository;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerDataAccessMapper dataAccessMapper;

    @Override
    public Optional<Customer> findCustomer(UUID customerId) {
        return customerJpaRepository.findById(customerId)
                .map(dataAccessMapper::customerEntityToCustomer);
    }
}
