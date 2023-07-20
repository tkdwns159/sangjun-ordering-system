package com.sangjun.payment.domain.valueobject.book;

import com.sangjun.payment.domain.entity.book.BookEntry;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.List;

@Embeddable
public class BookEntryList {

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
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
}
