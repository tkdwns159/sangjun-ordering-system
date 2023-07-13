package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.exception.BookDomainException;

import java.util.Objects;

public class TransactionValue {

    private TransactionValueType type;
    private Money amount;

    public TransactionValue(TransactionValueType type, Money amount) {
        this.type = Objects.requireNonNull(type, "Transaction value type must be non-null");
        this.amount = Objects.requireNonNull(amount, "Transaction value amount must be non-null");
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionValueType getType() {
        return type;
    }

    public void validate() {
        checkIfAmountIsPositive();
    }

    private void checkIfAmountIsPositive() {
        if (amount.equals(Money.ZERO)) {
            throw new BookDomainException("Transaction value amount must be greater than zero");
        }
    }

}
