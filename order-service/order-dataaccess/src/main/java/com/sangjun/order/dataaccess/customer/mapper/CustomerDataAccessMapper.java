package com.sangjun.order.dataaccess.customer.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.dataaccess.customer.entity.CustomerEntity;
import com.sangjun.order.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

    public Customer customerEntityToCustomer(CustomerEntity customerEntity) {
        return new Customer(new CustomerId(customerEntity.getId()));
    }

}
