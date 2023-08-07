package com.sangjun.restaurant.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.restaurant.domain.valueobject.PendingOrderId;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "pending_order", schema = "restaurant")
@Access(AccessType.FIELD)
public class PendingOrder extends BaseEntity<PendingOrderId> {
}
