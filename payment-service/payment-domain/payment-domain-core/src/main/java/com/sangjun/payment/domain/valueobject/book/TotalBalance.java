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

    protected TotalBalance() {
    }

    private TotalBalance(Money totalCreditAmount, Money totalDebitAmount) {
        this.totalCreditAmount = totalCreditAmount;
        this.totalDebitAmount = totalDebitAmount;
    }

    public static TotalBalance newInstance() {
        return new TotalBalance(Money.ZERO, Money.ZERO);
    }

    public TotalBalance addCredit(Money amount) {
        return new TotalBalance(this.totalCreditAmount.add(amount), this.totalDebitAmount);
    }

    public TotalBalance addDebit(Money amount) {
        return new TotalBalance(this.totalCreditAmount, this.totalDebitAmount.add(amount));
    }

    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }

    public Money getTotalDebitAmount() {
        return totalDebitAmount;
    }

    public Money getCurrentBalance() {
        return totalDebitAmount.subtract(totalCreditAmount);
    }
}
