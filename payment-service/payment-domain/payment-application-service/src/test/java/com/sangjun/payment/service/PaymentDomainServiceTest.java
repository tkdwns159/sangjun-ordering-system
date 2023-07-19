package com.sangjun.payment.service;

import com.sangjun.payment.domain.PaymentDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PaymentDomainServiceConfig.class)
public class PaymentDomainServiceTest {

    @Autowired
    private PaymentDomainService paymentDomainService;

    @Test
    void initiate_payment() {
    }
}
