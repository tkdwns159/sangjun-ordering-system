package com.sangjun.payment.service.ports.input.service.dto;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class BookEntryCreateRequest {
    private final UUID bookId;
    private final TransactionValueType transactionValueType;
    private final Money transactionValueAmount;
    private final String description;
}
