package com.patex.extlib;

import com.patex.opds.OPDSEntryI;
import com.patex.opds.OPDSLink;

import java.util.List;

/**
 *
 */
public class ExtLibFeed {

    private final String title;
    private final List<OPDSEntryI> entries;
    private final List<OPDSLink> links;


    public ExtLibFeed(String title, List<OPDSEntryI> entries, List<OPDSLink> links) {
        this.title = title;
        this.entries = entries;
        this.links = links;
    }

    public String getTitle() {
        return title;
    }


    public List<OPDSEntryI> getEntries() {
        return entries;
    }

    public List<OPDSLink> getLinks() {
        return links;
    }
}
