package com.patex.forever.opds.model;

import com.patex.forever.model.Res;
import com.patex.forever.opds.model.converter.OPDSAuthor;
import com.patex.forever.opds.model.converter.OPDSAuthorImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OPDSEntryBuilder {


    private final String id;
    private final Instant updated;
    private final Res title;
    private List<OPDSContent> content = new ArrayList<>();
    private List<OPDSLink> links = new ArrayList<>();
    private List<OPDSAuthor> authors = new ArrayList<>();


    public OPDSEntryBuilder(String id, Instant updated, Res title) {
        this.id = id;
        this.updated = updated;
        this.title = title;
    }

    public OPDSEntryBuilder addContent(OPDSContent content) {
        this.content.add(content);
        return this;
    }

    public OPDSEntryBuilder addContent(String content) {
        this.content.add(new OPDSContent(content));
        return this;
    }

    public OPDSEntryBuilder addLink(String href, String rel, String type) {
        this.links.add(new OPDSLink(href, rel, type));
        return this;
    }

    public OPDSEntryBuilder addLink(String href, String type) {
        this.links.add(new OPDSLink(href, type));
        return this;
    }

    public OPDSEntryBuilder addLink(OPDSLink link) {
        this.links.add(link);
        return this;
    }

    public OPDSEntryBuilder addAuthor(final String name, final String uri) {
        this.authors.add(new OPDSAuthorImpl(name, uri));
        return this;
    }

    public OPDSEntry build() {
        return new OPDSEntryImpl(id, updated, title,
                Collections.unmodifiableList(content),
                Collections.unmodifiableList(links),
                Collections.unmodifiableList(authors)
        );
    }

}
