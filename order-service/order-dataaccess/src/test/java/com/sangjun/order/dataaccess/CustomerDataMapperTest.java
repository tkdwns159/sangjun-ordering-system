package com.sangjun.order.dataaccess;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.order.dataaccess.customer.entity.CustomerEntity;
import com.sangjun.order.domain.entity.Customer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.sangjun.order.dataaccess.customer.mapper.CustomerDataMapstructMapper.MAPPER;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerDataMapperTest {

    @Test
    void customerToCustomerEntity() {
        Customer customer = new Customer(new CustomerId(UUID.randomUUID()));
        CustomerEntity entity = MAPPER.toCustomerEntity(customer);

        assertEquals(customer.getId().getValue(), entity.getId());
    }

    @Test
    void customerEntityToCustomer() {
        CustomerEntity entity = CustomerEntity.builder()
                .id(UUID.randomUUID())
                .build();
        Customer customer = MAPPER.toCustomer(entity);

        assertEquals(entity.getId(), customer.getId().getValue());
    }
}
