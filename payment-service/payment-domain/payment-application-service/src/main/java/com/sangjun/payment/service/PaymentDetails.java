package com.sangjun.payment.service;

import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PaymentDetails {
    private CreditEntry creditEntry;
    private List<CreditHistory> creditHistories;
    private List<String> failureMessages;
}
