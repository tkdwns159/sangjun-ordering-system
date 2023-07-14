package com.sangjun.payment.service.ports.input.service.dto;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BookEntryCreateRequest {
    private final Long bookId;
    private final TransactionValueType transactionValueType;
    private final Money transactionValueAmount;
    private final String description;
}
