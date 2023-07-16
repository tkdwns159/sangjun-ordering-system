package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class TotalBalance {
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_credit_amount"))
    private Money totalCreditAmount;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_debit_amount"))
    private Money totalDebitAmount;

    public TotalBalance() {
        this.totalCreditAmount = Money.ZERO;
        this.totalDebitAmount = Money.ZERO;
    }

    private TotalBalance(Money totalCreditAmount, Money totalDebitAmount) {
        this.totalCreditAmount = totalCreditAmount;
        this.totalDebitAmount = totalDebitAmount;
    }

    public TotalBalance addCredit(Money amount) {
        return new TotalBalance(this.totalCreditAmount.add(amount), this.totalDebitAmount);
    }

    public TotalBalance addDebit(Money amount) {
        return new TotalBalance(this.totalCreditAmount, this.totalDebitAmount.add(amount));
    }
}
