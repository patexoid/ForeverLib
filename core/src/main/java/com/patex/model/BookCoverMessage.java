package com.patex.model;

import com.patex.zombie.model.Book;
import com.patex.zombie.model.BookImage;
import com.patex.zombie.model.User;

import java.io.Serializable;

public record BookCoverMessage(long book, BookImage bookImage) implements Serializable {
}
