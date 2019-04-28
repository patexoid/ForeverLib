package com.patex.entities;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class BookFileID implements Serializable {

    private final long bookId;
    private final String filePath;
    private final int contentSize;

    public BookFileID(Book book) {
        bookId = book.getId();
        filePath = book.getFileResource().getFilePath();
        contentSize=book.getContentSize();
    }

}
