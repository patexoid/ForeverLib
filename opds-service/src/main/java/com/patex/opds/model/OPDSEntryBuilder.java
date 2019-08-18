package com.patex.opds.model;

import com.patex.utils.Res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OPDSEntryBuilder {


    private final String id;
    private final Res title;
    private  Instant updated;
    private List<OPDSContent> content = new ArrayList<>();
    private List<OPDSLink> links = new ArrayList<>();
    private List<OPDSAuthor> authors = new ArrayList<>();


    OPDSEntryBuilder(String id, String keyTitle, Object... objs) {
        this(id, new Res(keyTitle, objs));
    }

    OPDSEntryBuilder(String id, String keyTitle) {
        this(id, new Res(keyTitle));
    }

    OPDSEntryBuilder(String id, Res title) {
        this.id = id;

        this.title = title;
    }

    public OPDSEntryBuilder withUpdated(Instant updated) {
        this.updated = updated;
        return this;
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

    public OPDSEntryBuilder addLink(String href) {
        this.links.add(new OPDSLink(href, OPDSLink.OPDS_CATALOG));
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
