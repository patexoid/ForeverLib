package com.patex.opds;

import com.rometools.rome.feed.atom.Link;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OPDSMetadata {

    private final String title;
    private final String id;
    private final Date update;
    private final List<Link> otherLinks;

    public OPDSMetadata(String title, String id, Date update, List<Link> otherLinks) {
        this.title = title;
        this.id = id;
        this.update = update;
        this.otherLinks = otherLinks;
    }

    public OPDSMetadata(String title, String id, Date update) {
        this.title = title;
        this.id = id;
        this.update = update;
        otherLinks= Collections.emptyList();
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public Date getUpdated() {
        return update;
    }

    public List<Link> getOtherLinks() {
        return otherLinks;
    }
}
