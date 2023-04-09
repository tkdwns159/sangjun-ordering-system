package com.sangjun.order.service.dataaccess.customer.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "customers", schema = "customer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    private UUID id;

}
