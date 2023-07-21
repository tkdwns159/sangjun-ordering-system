package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.Money;

import javax.persistence.*;

import static java.util.Objects.requireNonNull;

@Embeddable
public class TransactionValue {

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionValueType type;
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "transaction_amount"))
    private Money amount;

    private TransactionValue(TransactionValueType type, Money amount) {
        this.type = type;
        this.amount = amount;
    }

    protected TransactionValue() {
        
    }

    public static TransactionValue of(TransactionValueType type, Money amount) {
        validate(type, amount);
        return new TransactionValue(type, amount);
    }

    private static void validate(TransactionValueType type, Money amount) {
        requireNonNull(type, "transactionValue");
        requireNonNull(amount, "amount");
        checkIfAmountIsPositive(amount);
    }

    private static void checkIfAmountIsPositive(Money amount) {
        if (amount.equals(Money.ZERO)) {
            throw new IllegalArgumentException("transaction value amount must be greater than zero");
        }
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionValueType getType() {
        return type;
    }


}
