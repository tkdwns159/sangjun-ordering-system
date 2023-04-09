package com.sangjun.restaurant.dataaccess.entity;

import com.sangjun.common.domain.valueobject.OrderApprovalStatus;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_approval", schema = "restaurant")
public class OrderApprovalEntity {

    @Id
    private UUID id;
    private UUID restaurantId;
    private UUID orderId;
    @Enumerated(EnumType.STRING)
    private OrderApprovalStatus status;

}
