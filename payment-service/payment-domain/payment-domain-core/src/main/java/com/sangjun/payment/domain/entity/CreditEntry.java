package com.sangjun.payment.domain.entity;

import com.sangjun.common.domain.entity.BaseEntity;
import com.sangjun.common.domain.valueobject.CustomerId;
import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.valueobject.CreditEntryId;

import javax.persistence.*;


@Entity
@Table(name = "credit_entry", schema = "payment")
@Access(AccessType.FIELD)
@AttributeOverride(name = "value", column = @Column(name = "id"))
public class CreditEntry extends BaseEntity<CreditEntryId> {
    @Embedded
    private CustomerId customerId;

    @Embedded
    private Money totalCreditAmount;

    protected CreditEntry() {
    }

    private CreditEntry(Builder builder) {
        setId(builder.id);
        customerId = builder.customerId;
        totalCreditAmount = builder.totalCreditAmount;
    }

    public static Builder builder(CustomerId customerId) {
        return new Builder(customerId);
    }

    public void addCreditAmount(Money amount) {
        this.totalCreditAmount = totalCreditAmount.add(amount);
    }

    public void subtractCreditAmount(Money amount) {
        this.totalCreditAmount = totalCreditAmount.subtract(amount);
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public static final class Builder {
        private CreditEntryId id;
        private final CustomerId customerId;
        private Money totalCreditAmount;

        private Builder(CustomerId customerId) {
            this.customerId = customerId;
        }

        public Builder id(CreditEntryId val) {
            id = val;
            return this;
        }

        public Builder totalCreditAmount(Money val) {
            totalCreditAmount = val;
            return this;
        }

        public CreditEntry build() {
            return new CreditEntry(this);
        }
    }
}
