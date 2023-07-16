package com.sangjun.payment.dataaccess.credithistory.repository;

import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.service.ports.output.repository.CreditHistoryRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditHistoryJpaRepository extends CreditHistoryRepository, Repository<CreditHistory, UUID> {
    @Override
    CreditHistory save(CreditHistory creditHistory);

    @Override
    Optional<List<CreditHistory>> findByCustomerId(UUID customerId);
}
