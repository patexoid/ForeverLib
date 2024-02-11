package com.patex.forever.model;

import java.io.Serializable;

public record BookCoverMessage(long book, BookImage bookImage) implements Serializable {
}
