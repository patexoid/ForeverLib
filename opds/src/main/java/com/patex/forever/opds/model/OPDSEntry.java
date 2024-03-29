package com.patex.forever.opds.model;

import com.patex.forever.model.Res;
import com.patex.forever.opds.model.converter.OPDSAuthor;

import java.time.Instant;
import java.util.List;

/**
 * Created by Alexey on 07.05.2017.
 */
public interface OPDSEntry {

    String getId();

    Instant getUpdated();

    Res getTitle();

    List<OPDSContent> getContent();

    List<OPDSLink> getLinks();

    List<OPDSAuthor> getAuthors();
}

