package com.patex.zombie.opds.model;

import com.patex.zombie.core.utils.Res;

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

    static OPDSEntryBuilder builder(String id, String keyTitle, Object... objs){
       return new OPDSEntryBuilder(id, keyTitle, objs);
    }


    static OPDSEntryBuilder builder(String id, String keyTitle){
        return new OPDSEntryBuilder(id, keyTitle);
    }

}

