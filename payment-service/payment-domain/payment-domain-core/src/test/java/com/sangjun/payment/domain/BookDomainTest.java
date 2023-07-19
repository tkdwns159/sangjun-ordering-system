package com.sangjun.payment.domain;

import com.sangjun.common.domain.valueobject.Money;
import com.sangjun.payment.domain.entity.book.Book;
import com.sangjun.payment.domain.entity.book.BookShelve;
import com.sangjun.payment.domain.valueobject.book.EntryIdType;
import com.sangjun.payment.domain.valueobject.book.TransactionValue;
import com.sangjun.payment.domain.valueobject.book.TransactionValueType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        // given
        Money money = Money.ZERO;

        // when, then
        assertThatThrownBy(() ->
                TransactionValue.of(TransactionValueType.DEBIT, Money.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addTransactionValueToBookTotalBalanceWhenEntryAdded() {
        // given
        BookShelve bookShelve = BookShelve.of("my", EntryIdType.UUID);
        UUID bookId = UUID.randomUUID();
        Book book = Book.of(bookShelve, bookId.toString());
        TransactionValue tv = TransactionValue.of(TransactionValueType.DEBIT, Money.of("1000"));

        // when
        book.addBookEntry(tv, "test");

        // then
        assertThat(book.getTotalBalance().getTotalDebitAmount())
                .isEqualTo(Money.of("1000"));
    }
}
