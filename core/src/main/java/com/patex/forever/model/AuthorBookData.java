package com.patex.forever.model;

import java.time.Instant;

public interface AuthorBookData {

    long getBookId();

    String getBookTitle();

    boolean getBookDuplicate();

    String getBookDescr();

    Instant getBookCreated();

    String getCoverType();

    Long getCoverId();

    Long getSequenceId();

    Integer getSeqOrder();

    String getSequenceName();

    Long getAuthorId();

    String getAuthorName();
}
