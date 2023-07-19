package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.payment.domain.entity.book.BookEntry;

import javax.persistence.Embeddable;
import java.util.List;

@Embeddable
public class BookEntryList {
    private final List<BookEntry> bookEntries;

    public BookEntryList(List<BookEntry> bookEntries) {
        this.bookEntries = bookEntries;
    }

    public void add(BookEntry bookEntry) {
        this.bookEntries.add(bookEntry);
    }

    public int getSize() {
        return bookEntries.size();
    }

    public BookEntry getLastEntry() {
        return this.bookEntries.get(this.bookEntries.size() - 1);
    }
}
