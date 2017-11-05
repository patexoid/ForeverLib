package com.patex.opds.converters;

import com.patex.opds.OPDSContent;
import com.patex.utils.Res;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexey on 07.05.2017.
 */
public interface OPDSEntryI {

    String getId();

    Date getUpdated();

    Res getTitle();

    default Optional<List<OPDSContent>> getContent() {
        return Optional.empty();
    }

    List<OPDSLink> getLinks();

    default Optional<List<OPDSAuthor>> getAuthors() {
        return Optional.empty();
    }
}

