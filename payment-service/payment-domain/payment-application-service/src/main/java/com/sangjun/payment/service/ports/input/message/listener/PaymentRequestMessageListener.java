package com.sangjun.payment.service.ports.input.message.listener;

import com.sangjun.payment.service.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListener {
    private final PaymentEventShooter paymentEventShooter;

    public void completePayment(PaymentRequest paymentRequest) {
    }

    public void cancelPayment(PaymentRequest paymentRequest) {
    }
}
