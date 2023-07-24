package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.payment.domain.entity.book.BookEntry;

import javax.persistence.*;
import java.util.List;

@Embeddable
public class BookEntryList {

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "book_id")
    private List<BookEntry> bookEntries;

    public BookEntryList(List<BookEntry> bookEntries) {
        this.bookEntries = bookEntries;
    }

    protected BookEntryList() {
    }

    public void add(BookEntry bookEntry) {
        this.bookEntries.add(bookEntry);
    }

    public int getSize() {
        return bookEntries.size();
    }
}
