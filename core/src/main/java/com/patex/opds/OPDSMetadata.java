package com.patex.opds;

import com.patex.utils.Res;
import com.rometools.rome.feed.atom.Link;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class OPDSMetadata {

    private final Res title;
    private final String id;
    private final Instant update;
    private final List<Link> otherLinks;

    public OPDSMetadata(Res title, String id, Instant update, List<Link> otherLinks) {
        this.title = title;
        this.id = id;
        this.update = update;
        this.otherLinks = otherLinks;
    }

    public OPDSMetadata(Res title, String id, Instant update) {
        this.title = title;
        this.id = id;
        this.update = update;
        otherLinks = Collections.emptyList();
    }

    public Res getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public Instant getUpdated() {
        return update;
    }

    public List<Link> getOtherLinks() {
        return otherLinks;
    }
}
