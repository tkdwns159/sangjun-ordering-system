package com.sangjun.payment.service.ports.input.message.listener;

import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.payment.domain.PaymentCancelDomainService;
import com.sangjun.payment.domain.PaymentInitDomainService;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentCancelledEvent;
import com.sangjun.payment.domain.event.PaymentEvent;
import com.sangjun.payment.domain.event.PaymentFailedEvent;
import com.sangjun.payment.domain.exception.IllegalPaymentStateException;
import com.sangjun.payment.service.PaymentEventShooter;
import com.sangjun.payment.service.RepositoryManager;
import com.sangjun.payment.service.dto.PaymentRequest;
import com.sangjun.payment.service.ports.output.repository.BookOwnerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.sangjun.common.utils.CommonConstants.ZONE_ID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRequestMessageListener {
    private final PaymentEventShooter paymentEventShooter;
    private final PaymentInitDomainService paymentInitDomainService;
    private final PaymentCancelDomainService paymentCancelDomainService;
    private final RepositoryManager repositoryManager;

    @Transactional
    public void completePayment(PaymentRequest paymentRequest) {
        final Payment payment = PaymentRequestMapper.MAPPER.toPayment(paymentRequest);
        final Book customerBook =
                repositoryManager.getBook(BookOwnerType.CUSTOMER, payment.getCustomerId().getValue());
        final Book restaurantBook =
                repositoryManager.getBook(BookOwnerType.RESTAURANT, payment.getRestaurantId().getValue());

        final PaymentEvent paymentEvent = executePayment(payment, customerBook, restaurantBook);
        repositoryManager.save(paymentEvent.getPayment());
        paymentEventShooter.fire(paymentEvent);
    }

    private PaymentEvent executePayment(Payment payment, Book customerBook, Book restaurantBook) {
        try {
            return paymentInitDomainService.initPayment(payment.clone(), customerBook, restaurantBook);
        } catch (IllegalPaymentStateException ex) {
            payment.markAsFailed();
            return new PaymentFailedEvent(payment, ZonedDateTime.now(ZoneId.of(ZONE_ID)));
        }
    }

    @Transactional
    public void cancelPayment(PaymentRequest paymentRequest) {
        final OrderId orderId = new OrderId(UUID.fromString(paymentRequest.getOrderId()));
        final Payment payment =
                repositoryManager.getPayment(orderId);
        final Book customerBook =
                repositoryManager.getBook(BookOwnerType.CUSTOMER, payment.getCustomerId().getValue());
        final Book restaurantBook =
                repositoryManager.getBook(BookOwnerType.RESTAURANT, payment.getRestaurantId().getValue());

        final PaymentCancelledEvent paymentCancelledEvent =
                paymentCancelDomainService.cancelPayment(payment, restaurantBook, customerBook);
        repositoryManager.save(paymentCancelledEvent.getPayment());
        paymentEventShooter.fire(paymentCancelledEvent);
    }
}
