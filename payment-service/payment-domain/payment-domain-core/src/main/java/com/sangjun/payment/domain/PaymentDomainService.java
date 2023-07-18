package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.common.domain.valueobject.OrderId;
import com.sangjun.common.domain.valueobject.RestaurantId;
import com.sangjun.payment.domain.entity.CreditEntry;
import com.sangjun.payment.domain.entity.CreditHistory;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.payment.Payment;
import com.sangjun.payment.domain.event.PaymentEvent;

import java.util.List;

public interface PaymentDomainService {


    PaymentEvent initiatePayment(OrderId orderId,
                                 CustomerId customerId,
                                 RestaurantId restaurantId,
                                 Money price,
                                 Book book,
                                 List<String> failureMessages);

    PaymentEvent cancelPayment(Payment payment,
                               CreditEntry creditEntry,
                               List<CreditHistory> histories,
                               List<String> failureMessages);
}
