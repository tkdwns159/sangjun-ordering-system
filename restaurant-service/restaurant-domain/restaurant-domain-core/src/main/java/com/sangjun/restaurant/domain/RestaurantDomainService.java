package com.sangjun.restaurant.domain;

import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.event.OrderApprovalEvent;

import java.util.List;

public interface RestaurantDomainService {
    OrderApprovalEvent validateOrder(Restaurant restaurant,
                                     List<String> failureMessages);

}
