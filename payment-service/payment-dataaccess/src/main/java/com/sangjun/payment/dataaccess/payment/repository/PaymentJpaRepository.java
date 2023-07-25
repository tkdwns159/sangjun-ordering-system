package com.sangjun.payment.dataaccess.payment.repository;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.valueobject.payment.PaymentId;
import com.sangjun.payment.service.ports.output.repository.PaymentRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface PaymentJpaRepository extends PaymentRepository, Repository<Payment, PaymentId> {
    @Override
    Payment save(Payment payment);

    @Override
    Optional<Payment> findByOrderId(OrderId orderId);
}
