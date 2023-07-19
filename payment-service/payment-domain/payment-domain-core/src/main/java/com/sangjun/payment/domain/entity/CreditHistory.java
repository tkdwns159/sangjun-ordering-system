package com.sangjun.payment.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.CreditHistoryId;
import com.sangjun.payment.domain.valueobject.TransactionType;

import javax.persistence.*;

@Entity
@Table(name = "credit_history", schema = "payment")
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "id"))
public class CreditHistory extends BaseEntity<CreditHistoryId> {
    @Embedded
    private final CustomerId customerId;
    @Embedded
    private final Money amount;
    @Enumerated(EnumType.STRING)
    private final TransactionType transactionType;

    private CreditHistory(Builder builder) {
        setId(builder.id);
        customerId = builder.customerId;
        amount = builder.amount;
        transactionType = builder.transactionType;
    }

    public static Builder builder(CustomerId customerId, Money amount, TransactionType transactionType) {
        return new Builder(customerId, amount, transactionType);
    }


    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }


    public static final class Builder {
        private CreditHistoryId id;
        private final CustomerId customerId;
        private final Money amount;
        private final TransactionType transactionType;

        private Builder(CustomerId customerId, Money amount, TransactionType transactionType) {
            this.customerId = customerId;
            this.amount = amount;
            this.transactionType = transactionType;
        }

        public Builder id(CreditHistoryId val) {
            id = val;
            return this;
        }

        public CreditHistory build() {
            return new CreditHistory(this);
        }
    }
}
