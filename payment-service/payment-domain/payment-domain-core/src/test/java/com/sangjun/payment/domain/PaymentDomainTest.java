package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.*;
import com.sangjun.payment.domain.entity.payment.Payment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PaymentDomainTest {

    @Test
    void paymentMustNotHaveIdAndCreatedAtAndPaymentStatusWhenJustHasCreated() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        assertThat(payment.getId())
                .isNull();
        assertThat(payment.getCreatedAt())
                .isNull();
    }

    @Test
    void priceMustBeBiggerThanZeroWhenInitializingPayment() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("0");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        Assertions.assertThatThrownBy(payment::initialize)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void setPaymentIdAndCreatedAtWhileInitializingPayment() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();
        payment.initialize();

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    void paymentStatusMustBeReadyWhenCompletingPayment() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        payment.initialize();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.READY);

        payment.complete();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void paymentStatusMustBeCompletedWhenCancellingPayment() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        payment.initialize();
        payment.complete();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);

        payment.cancel();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void givenReadyPaymentWhenCancelPaymentThenThrowException() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        payment.initialize();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.READY);

        assertThatThrownBy(payment::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenFailedPaymentWhenMarkingPaymentAsFailedThenThrowException() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Money price = Money.of("1000");

        Payment payment = Payment.builder()
                .restaurantId(new RestaurantId(restaurantId))
                .orderId(new OrderId(orderId))
                .customerId(new CustomerId(customerId))
                .price(price)
                .build();

        payment.initialize();
        payment.complete();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.COMPLETED);

        payment.markAsFailed();
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.FAILED);

        assertThatThrownBy(payment::markAsFailed)
                .isInstanceOf(IllegalStateException.class);
    }

}
