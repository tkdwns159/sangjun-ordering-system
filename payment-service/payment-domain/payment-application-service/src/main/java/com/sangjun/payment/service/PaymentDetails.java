package com.sangjun.payment.service;

import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import lombok.Builder;

import java.util.List;

@Builder
public class PaymentDetails {
    CreditEntry creditEntry;
    List<CreditHistory> creditHistories;
    List<String> failureMessages;
}
