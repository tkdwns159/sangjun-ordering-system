package com.sangjun.order.domain.service.ports.output.service.customer;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.service.ports.output.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerCheckServiceImpl implements CustomerCheckService {
    private final CustomerRepository customerRepository;

    @Override
    public boolean existsById(CustomerId customerId) {
        return customerRepository
                .findById(customerId.getValue())
                .isPresent();
    }
}
