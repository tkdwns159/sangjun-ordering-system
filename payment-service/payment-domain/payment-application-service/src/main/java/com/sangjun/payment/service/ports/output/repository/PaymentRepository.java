package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.entity.payment.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(OrderId orderId);
}
