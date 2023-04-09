package com.sangjun.restaurant.application.ports.output.message.repository;

import com.sangjun.restaurant.domain.entity.OrderApproval;

public interface OrderApprovalRepository {
    OrderApproval save(OrderApproval orderApproval);
}
