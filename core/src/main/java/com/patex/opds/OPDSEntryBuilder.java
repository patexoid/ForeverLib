package com.patex.opds;

import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSAuthorImpl;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSEntryImpl;
import com.patex.opds.converters.OPDSLink;
import com.patex.utils.Res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OPDSEntryBuilder {


    private final String id;
    private final Date updated;
    private final Res title;
    private List<OPDSContent> content = new ArrayList<>();
    private List<OPDSLink> links = new ArrayList<>();
    private List<OPDSAuthor> authors = new ArrayList<>();


    public OPDSEntryBuilder(String id, Date updated, Res title) {
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