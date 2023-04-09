package com.sangjun.restaurant.dataaccess.adapter;

import com.sangjun.restaurant.application.ports.output.message.repository.OrderApprovalRepository;
import com.sangjun.restaurant.dataaccess.mapper.RestaurantDataAccessMapper;
import com.sangjun.restaurant.dataaccess.repository.OrderApprovalJpaRepository;
import com.sangjun.restaurant.domain.entity.OrderApproval;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderApprovalRepositoryImpl implements OrderApprovalRepository {
    private final RestaurantDataAccessMapper restaurantDataAccessMapper;
    private final OrderApprovalJpaRepository orderApprovalJpaRepository;

    @Override
    public OrderApproval save(OrderApproval orderApproval) {
        return restaurantDataAccessMapper.orderApprovalEntityToOrderApproval(
                orderApprovalJpaRepository.save(
                        restaurantDataAccessMapper.orderApprovalToOrderApprovalEntity(orderApproval))
        );
    }
}
