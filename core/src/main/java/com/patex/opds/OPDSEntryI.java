package com.patex.opds;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexey on 07.05.2017.
 */
public interface OPDSEntryI {

    String getId();

    default Optional<Date> getUpdated() {
        return Optional.empty();
    }

    String getTitle();

    default Optional<List<String>> getContent() {
        return Optional.empty();
    }

    List<OPDSLink> getLinks();

    default Optional<List<OPDSAuthor>> getAuthors() {
        return Optional.empty();
    }
}

