package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.CreditEntry;

import java.util.Optional;
import java.util.UUID;

public interface CreditEntryRepository {
    CreditEntry save(CreditEntry creditEntry);

    Optional<CreditEntry> findByCustomerId(UUID customerId);
}
