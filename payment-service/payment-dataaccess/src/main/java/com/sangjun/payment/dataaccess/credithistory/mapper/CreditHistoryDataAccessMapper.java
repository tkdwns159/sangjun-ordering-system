package com.sangjun.payment.dataaccess.credithistory.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.dataaccess.credithistory.entity.CreditHistoryEntity;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import org.springframework.stereotype.Component;

@Component
public class CreditHistoryDataAccessMapper {

    public CreditHistory creditHistoryEntityToCreditHistory(CreditHistoryEntity creditHistoryEntity) {
        return CreditHistory.builder(
                        new CustomerId(creditHistoryEntity.getCustomerId()),
                        new Money(creditHistoryEntity.getAmount()),
                        creditHistoryEntity.getType()
                )
                .id(new CreditHistoryId(creditHistoryEntity.getId()))
                .build();
    }

    public CreditHistoryEntity creditHistoryToCreditHistoryEntity(CreditHistory creditHistory) {
        return CreditHistoryEntity.builder()
                .id(creditHistory.getId().getValue())
                .customerId(creditHistory.getCustomerId().getValue())
                .amount(creditHistory.getAmount().getAmount())
                .type(creditHistory.getTransactionType())
                .build();
    }

}
