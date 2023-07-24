package com.sangjun.payment.container;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.input.message.listener.PaymentRequestMapper;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentRequestMapperTest {

    @Test
    void test() {
        //given
        UUID orderId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("2039");

        //when
        Payment payment = PaymentRequestMapper
                .MAPPER
                .toPayment(PaymentRequest.builder()
                        .orderId(orderId.toString())
                        .customerId(customerId.toString())
                        .restaurantId(restaurantId.toString())
                        .paymentStatus(PaymentStatus.PENDING)
                        .price(price.getAmount())
                        .build());
        //then
        assertThat(payment)
                .isEqualTo(Payment.builder()
                        .orderId(new OrderId(orderId))
                        .restaurantId(new RestaurantId(restaurantId))
                        .customerId(new CustomerId(customerId))
                        .price(price)
                        .build());
    }


}
