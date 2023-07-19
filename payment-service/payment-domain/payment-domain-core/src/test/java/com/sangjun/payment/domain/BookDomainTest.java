package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class BookDomainTest {

    @Test
    void bookShelveNameMustNotBeEmptyString() {
        // given
        String name = "";

        // when, then
        assertThatThrownBy(() ->
                BookShelve.of(name, EntryIdType.UUID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transactionValueMustNotHaveZeroMoney() {
        // when, then
        assertThatThrownBy(() ->
                TransactionValue.of(TransactionValueType.DEBIT, Money.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
