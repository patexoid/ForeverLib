package com.patex.zombie.opds.model;

import com.patex.zombie.core.utils.Res;

import java.time.Instant;
import java.util.List;

/**
 * Created by Alexey on 08.06.2017.
 */
class OPDSEntryImpl implements OPDSEntry {

    private final String id;
    private final Instant updated;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final List<OPDSAuthor> authors;

    OPDSEntryImpl(String id, Instant updated, Res title, List<OPDSContent> content, List<OPDSLink> links, List<OPDSAuthor> authors) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        this.content = content;
        this.links = links;
        this.authors = authors;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instant getUpdated() {
        return updated;
    }

    @Override
    public Res getTitle() {
        return title;
    }

    @Override
    public List<OPDSContent> getContent() {
        return content;
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }

    @Override
    public List<OPDSAuthor> getAuthors() {
        return authors;
    }
}
