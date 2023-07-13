package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.common.domain.valueobject.Money;

public class TotalBalance {
    private Money totalCreditAmount;
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
