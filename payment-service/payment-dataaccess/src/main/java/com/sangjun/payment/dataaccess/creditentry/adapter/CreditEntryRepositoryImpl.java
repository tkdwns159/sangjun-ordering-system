package com.sangjun.payment.dataaccess.creditentry.adapter;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.payment.dataaccess.creditentry.mapper.CreditEntryDataAccessMapper;
import com.sangjun.payment.dataaccess.creditentry.repository.CreditEntryJpaRepository;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.service.ports.output.repository.CreditEntryRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CreditEntryRepositoryImpl implements CreditEntryRepository {

    private final CreditEntryJpaRepository creditEntryJpaRepository;
    private final CreditEntryDataAccessMapper creditEntryDataAccessMapper;

    public CreditEntryRepositoryImpl(CreditEntryJpaRepository creditEntryJpaRepository,
                                     CreditEntryDataAccessMapper creditEntryDataAccessMapper) {
        this.creditEntryJpaRepository = creditEntryJpaRepository;
        this.creditEntryDataAccessMapper = creditEntryDataAccessMapper;
    }

    @Override
    public CreditEntry save(CreditEntry creditEntry) {
        return creditEntryDataAccessMapper
                .creditEntryEntityToCreditEntry(creditEntryJpaRepository
                        .save(creditEntryDataAccessMapper.creditEntryToCreditEntryEntity(creditEntry)));
    }

    @Override
    public Optional<CreditEntry> findByCustomerId(UUID customerId) {
        return creditEntryJpaRepository
                .findByCustomerId(customerId)
                .map(creditEntryDataAccessMapper::creditEntryEntityToCreditEntry);
    }
}
