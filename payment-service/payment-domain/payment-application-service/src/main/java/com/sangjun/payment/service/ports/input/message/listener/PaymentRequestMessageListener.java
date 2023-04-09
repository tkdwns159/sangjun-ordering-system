package com.sangjun.payment.service.ports.input.message.listener;

import com.sangjun.payment.service.dto.PaymentRequest;

public interface PaymentRequestMessageListener {

    void completePayment(PaymentRequest paymentRequest);

    void cancelPayment(PaymentRequest paymentRequest);

}
