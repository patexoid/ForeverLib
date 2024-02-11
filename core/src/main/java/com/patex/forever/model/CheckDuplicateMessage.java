package com.patex.forever.model;

import java.io.Serializable;

public record CheckDuplicateMessage(long book, String user) implements Serializable {

}
