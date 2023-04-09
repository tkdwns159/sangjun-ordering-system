package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(UUID orderId);
}
