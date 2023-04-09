package com.sangjun.order.service.dataaccess.customer.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.domain.entity.Customer;
import com.sangjun.order.service.dataaccess.customer.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()));
    }

}
