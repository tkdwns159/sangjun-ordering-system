package com.sangjun.payment.dataaccess.creditentry.mapper;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.dataaccess.creditentry.entity.CreditEntryEntity;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.valueobject.CreditEntryId;
import org.springframework.stereotype.Component;

@Component
public class CreditEntryDataAccessMapper {

    public CreditEntry creditEntryEntityToCreditEntry(CreditEntryEntity creditEntryEntity) {
        return CreditEntry.builder(new CustomerId(creditEntryEntity.getCustomerId()))
                .id(new CreditEntryId(creditEntryEntity.getId()))
                .totalCreditAmount(new Money(creditEntryEntity.getTotalCreditAmount()))
                .build();
    }

    public CreditEntryEntity creditEntryToCreditEntryEntity(CreditEntry creditEntry) {
        return CreditEntryEntity.builder()
                .id(creditEntry.getId().getValue())
                .customerId(creditEntry.getCustomerId().getValue())
                .totalCreditAmount(creditEntry.getTotalCreditAmount().getAmount())
                .build();
    }

}
