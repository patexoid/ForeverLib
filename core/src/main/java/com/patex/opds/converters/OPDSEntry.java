package com.patex.opds.converters;

import com.patex.opds.OPDSContent;
import com.patex.utils.Res;

import java.util.Date;
import java.util.List;

/**
 * Created by Alexey on 07.05.2017.
 */
public interface OPDSEntry {

    String getId();

    Date getUpdated();

    Res getTitle();

    List<OPDSContent> getContent();

    List<OPDSLink> getLinks();

    List<OPDSAuthor> getAuthors();
}

