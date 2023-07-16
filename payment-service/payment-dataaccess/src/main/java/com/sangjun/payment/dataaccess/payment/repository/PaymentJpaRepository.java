package com.sangjun.payment.dataaccess.payment.repository;

import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends PaymentRepository, Repository<Payment, UUID> {
    @Override
    Payment save(Payment payment);

    @Override
    Optional<Payment> findByOrderId(UUID orderId);
}
