package com.patex.opds.converters;

import com.patex.opds.OPDSContent;
import com.patex.utils.Res;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 08.06.2017.
 */
public class OPDSEntryImpl implements OPDSEntry {

    private final String id;
    private final Date updated;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final List<OPDSAuthor> authors;

    public OPDSEntryImpl(String id, Date updated, Res title, List<OPDSContent> content, List<OPDSLink> links, List<OPDSAuthor> authors) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        this.content = content;
        this.links = links;
        this.authors = authors;
    }

    public OPDSEntryImpl(String id, Date updated, Res title, List<String> content, String... links) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if (content == null) {
            this.content = Collections.emptyList();
        } else {
            this.content = content.stream().filter(Objects::nonNull).map(OPDSContent::new).collect(Collectors.toList());
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
        authors = Collections.emptyList();
    }

    public OPDSEntryImpl(String id, Date updated, Res title, String content, String... links) {
        this(id, updated, title, Collections.singletonList(content), links);
    }

    public OPDSEntryImpl(String id, Res title, String content, String... links) {
        this(id, null, title, Collections.singletonList(content), links);
    }

    public OPDSEntryImpl(String id, Date updated, Res title, String content, OPDSLink link) {
        this(id, updated, title,
                Collections.singletonList(new OPDSContent(content)),
                Collections.singletonList(link),
                Collections.emptyList());
    }

    public OPDSEntryImpl(String id, Res title, OPDSLink link) {
        this(id, null, title, (String) null, link);
    }

    public OPDSEntryImpl(String id, Date updated, Res title, OPDSContent content, String... links) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if (content == null) {
            this.content = Collections.emptyList();
        } else {
            this.content = Collections.singletonList(content);
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
        authors = Collections.emptyList();
    }

    public OPDSEntryImpl(String id, Date updated, Res title, OPDSContent content, OPDSLink link) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if (content == null) {
            this.content = Collections.emptyList();
        } else {
            this.content = Collections.singletonList(content);
        }
        this.links = Collections.singletonList(link);
        authors = Collections.emptyList();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getUpdated() {
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
