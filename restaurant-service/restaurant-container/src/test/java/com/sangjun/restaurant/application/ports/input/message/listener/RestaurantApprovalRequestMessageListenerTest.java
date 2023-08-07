package com.sangjun.restaurant.application.ports.input.message.listener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
class RestaurantApprovalRequestMessageListenerTest {

    @Test
    void 상품가격_총합과_지불가격이_같아야한다() {

    }
}