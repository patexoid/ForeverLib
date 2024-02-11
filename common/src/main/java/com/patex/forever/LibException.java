package com.patex.forever;

/**
 * Created by Alexey on 13.03.2016.
 */
public class LibException extends RuntimeException {

    public LibException() {
    }

    public LibException(String message) {
        super(message);
    }

    public LibException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibException(Throwable cause) {
        super(cause);
    }

    public LibException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
