package com.sangjun.order.dataaccess.customer.mapper;

import com.sangjun.order.dataaccess.customer.entity.CustomerEntity;
import com.sangjun.order.domain.entity.Customer;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomerDataMapstructMapper {
    CustomerDataMapstructMapper MAPPER = Mappers.getMapper(CustomerDataMapstructMapper.class);

    @Mapping(target = "id", source = "id.value")
    CustomerEntity toCustomerEntity(Customer customer);

    @InheritInverseConfiguration
    Customer toCustomer(CustomerEntity customerEntity);

}
