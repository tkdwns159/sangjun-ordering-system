package com.sangjun.payment.dataaccess.payment.mapper;


import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.dataaccess.payment.entity.PaymentEntity;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.payment.PaymentId;
import org.springframework.stereotype.Component;

@Component
public class PaymentDataAccessMapper {

    public PaymentEntity paymentToPaymentEntity(Payment payment) {
        return PaymentEntity.builder()
                .id(payment.getId().getValue())
                .customerId(payment.getCustomerId().getValue())
                .orderId(payment.getOrderId().getValue())
                .price(payment.getPrice().getAmount())
                .status(payment.getPaymentStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public Payment paymentEntityToPayment(PaymentEntity paymentEntity) {
        return Payment.builder(
                        new OrderId(paymentEntity.getOrderId()),
                        new CustomerId(paymentEntity.getCustomerId()),
                        new Money(paymentEntity.getPrice())
                )
                .id(new PaymentId(paymentEntity.getId()))
                .createdAt(paymentEntity.getCreatedAt())
                .paymentStatus(paymentEntity.getStatus())
                .build();
    }

}
