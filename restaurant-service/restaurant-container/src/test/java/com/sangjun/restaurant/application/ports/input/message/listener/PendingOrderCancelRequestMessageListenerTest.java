package com.sangjun.restaurant.application.ports.input.message.listener;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.restaurant.application.dto.PendingOrderCancelRequest;
import com.sangjun.restaurant.application.ports.output.message.repository.PendingOrderRepository;
import com.sangjun.restaurant.application.ports.output.message.repository.RestaurantRepository;
import com.sangjun.restaurant.domain.entity.PendingOrder;
import com.sangjun.restaurant.domain.entity.Restaurant;
import com.sangjun.restaurant.domain.valueobject.PendingOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@SpringBootTest(classes = TestConfig.class)
class PendingOrderCancelRequestMessageListenerTest {
    @Autowired
    private PendingOrderRepository pendingOrderRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;
    @Autowired
    private PendingOrderCancelRequestMessageListener listener;

    OrderId orderId = new OrderId(UUID.randomUUID());
    Restaurant restaurant;

    @BeforeEach
    void init() {
        restaurant = Restaurant.builder()
                .isActive(true)
                .build();
        restaurantRepository.save(restaurant);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
    }

    @Test
    void 승인전이면_취소처리() {
        //given
        PendingOrder pendingOrder = PendingOrder.builder()
                .orderId(orderId)
                .restaurantId(restaurant.getId())
                .status(PendingOrderStatus.PENDING)
                .build();
        PendingOrder savedPendingOrder = pendingOrderRepository.save(pendingOrder);

        //when
        listener.cancelPendingOrder(PendingOrderCancelRequest.builder()
                .orderId(orderId)
                .build());

        //then
        assertThat(savedPendingOrder.getStatus())
                .isEqualTo(PendingOrderStatus.CANCELLED);
    }
}