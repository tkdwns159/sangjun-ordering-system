package com.sangjun.order.dataaccess.customer.entity;

import lombok.*;

import javax.persistence.Id;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerEntity {

    @Id
    private UUID id;

}
