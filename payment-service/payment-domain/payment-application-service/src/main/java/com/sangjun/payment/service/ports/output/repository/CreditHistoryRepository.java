package com.sangjun.payment.service.ports.output.repository;

import com.sangjun.payment.domain.entity.CreditHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreditHistoryRepository {
    CreditHistory save(CreditHistory creditHistory);

    Optional<List<CreditHistory>> findByCustomerId(UUID customerId);
}
