package com.patex.model;

import java.io.Serializable;

public record CheckDuplicateMessage(long book, String user) implements Serializable {

}
