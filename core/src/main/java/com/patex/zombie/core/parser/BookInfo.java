package com.patex.zombie.core.parser;

import com.patex.zombie.core.entities.BookEntity;


public class BookInfo {

    private BookEntity book;

    private String coverage;
    private BookImage bookImage;

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public String getCoverpageImageHref() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public BookImage getBookImage() {
        return bookImage;
    }

    public void setBookImage(BookImage bookImage) {
        this.bookImage = bookImage;
    }
}
