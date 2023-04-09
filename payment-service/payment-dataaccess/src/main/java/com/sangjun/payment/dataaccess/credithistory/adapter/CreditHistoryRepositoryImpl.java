package com.sangjun.payment.dataaccess.credithistory.adapter;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.payment.dataaccess.credithistory.entity.CreditHistoryEntity;
import com.sangjun.payment.dataaccess.credithistory.mapper.CreditHistoryDataAccessMapper;
import com.sangjun.payment.dataaccess.credithistory.repository.CreditHistoryJpaRepository;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.service.ports.output.repository.CreditHistoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class CreditHistoryRepositoryImpl implements CreditHistoryRepository {

    private final CreditHistoryJpaRepository creditHistoryJpaRepository;
    private final CreditHistoryDataAccessMapper creditHistoryDataAccessMapper;

    public CreditHistoryRepositoryImpl(CreditHistoryJpaRepository creditHistoryJpaRepository,
                                       CreditHistoryDataAccessMapper creditHistoryDataAccessMapper) {
        this.creditHistoryJpaRepository = creditHistoryJpaRepository;
        this.creditHistoryDataAccessMapper = creditHistoryDataAccessMapper;
    }

    @Override
    public CreditHistory save(CreditHistory creditHistory) {
        return creditHistoryDataAccessMapper.creditHistoryEntityToCreditHistory(creditHistoryJpaRepository
                .save(creditHistoryDataAccessMapper.creditHistoryToCreditHistoryEntity(creditHistory)));
    }

    @Override
    public Optional<List<CreditHistory>> findByCustomerId(UUID customerId) {
        Optional<List<CreditHistoryEntity>> creditHistory =
                creditHistoryJpaRepository.findByCustomerId(customerId);
        return creditHistory
                .map(creditHistoryList ->
                        creditHistoryList.stream()
                                .map(creditHistoryDataAccessMapper::creditHistoryEntityToCreditHistory)
                                .collect(Collectors.toList()));
    }
}
