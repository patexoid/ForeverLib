package com.patex.opds.converters;

import com.patex.opds.OPDSContent;
import com.patex.utils.Res;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 08.06.2017.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class OPDSEntryImpl implements OPDSEntryI {

    private final String id;
    private final Date updated;
    private final Res title;
    private final Optional<List<OPDSContent>> content;
    private final List<OPDSLink> links;


    public OPDSEntryImpl(String id, Res title, OPDSLink link) {
        this.id = id;
        this.title = title;
        this.links = Collections.singletonList(link);
        updated = null;
        content = Optional.empty();
    }

    public OPDSEntryImpl(String id, Date updated, Res title, String content, OPDSLink link) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        this.content = Optional.of(Collections.singletonList(new OPDSContent(content)));
        this.links = Collections.singletonList(link);
    }

    public OPDSEntryImpl(String id, Date updated, Res title, List<String> content, String... links) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if (content == null) {
            this.content = Optional.empty();
        } else {
            this.content = Optional.of(content.stream().map(OPDSContent::new).collect(Collectors.toList()));
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
    }

    public OPDSEntryImpl(String id, Date updated, Res title, String content, String... links) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if (content == null) {
            this.content = Optional.empty();
        } else {
            this.content = Optional.of(Collections.singletonList(new OPDSContent(content)));
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
    }


    public OPDSEntryImpl(String id, Res title, String content, String... links) {
        this.id = id;
        this.updated=null;
        this.title = title;
        if (content == null) {
            this.content = Optional.empty();
        } else {
            this.content = Optional.of(Collections.singletonList(new OPDSContent(content)));
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
    }

    public OPDSEntryImpl(String id, Date updated, Res title, OPDSContent content, String... links) {
        this.id = id;
        this.updated = updated;
        this.title = title;
        if(content==null){
            this.content = Optional.empty();
        } else {
            this.content = Optional.of(Collections.singletonList(content));
        }
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
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
    public Optional<List<OPDSContent>> getContent() {
        return content;
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
