package com.sangjun.payment.dataaccess.creditentry.repository;

import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.service.ports.output.repository.CreditEntryRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface CreditEntryJpaRepository extends CreditEntryRepository, Repository<CreditEntry, UUID> {
    @Override
    CreditEntry save(CreditEntry creditEntry);

    @Override
    Optional<CreditEntry> findByCustomerId(UUID customerId);
}
