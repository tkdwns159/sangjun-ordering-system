package com.sangjun.order.domain.service.ports.output.service.customer;

import com.sangjun.common.domain.valueobject.CustomerId;

public interface CustomerCheckService {
    boolean existsById(CustomerId customerId);
}
