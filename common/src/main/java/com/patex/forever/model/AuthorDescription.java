package com.patex.forever.model;

import java.time.Instant;

public interface AuthorDescription {


    Long getId();

    String getName();

    String getDescr();

    Instant getUpdated();

    int getBookCount();

    int getSequenceCount();

    int getSequenceBookCount();

    Instant getSequenceUpdated();

    int getNoSequenceBookCount();

    Instant getNoSequenceUpdated();
}
