package com.patex.opds;

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
    private final Optional<Date> updated;
    private final String title;
    private final Optional<List<String>> content;
    private final List<OPDSLink> links;


    public OPDSEntryImpl(String id, String title, OPDSLink link) {
        this.id = id;
        this.title = title;
        this.links = Collections.singletonList(link);
        updated=Optional.empty();
        content=Optional.empty();
    }

    public OPDSEntryImpl(String id, Date updated, String title, String content, OPDSLink link) {
        this.id = id;
        this.updated = Optional.ofNullable(updated);
        this.title = title;
        this.content = Optional.of(Collections.singletonList(content));
        this.links = Collections.singletonList(link);
    }

    public OPDSEntryImpl(String id, Date updated, String title, List<String> content, String... links) {
        this.id = id;
        this.updated = Optional.ofNullable(updated);
        this.title = title;
        this.content = Optional.ofNullable(content);
        this.links = Arrays.stream(links).map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).
                collect(Collectors.toList());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Optional<Date> getUpdated() {
        return updated;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Optional<List<String>> getContent() {
        return content;
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
