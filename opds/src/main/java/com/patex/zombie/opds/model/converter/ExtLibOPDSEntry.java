package com.patex.zombie.opds.model.converter;

import com.patex.zombie.model.Res;
import com.patex.zombie.opds.model.OPDSContent;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.rometools.rome.feed.synd.SyndEntry;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExtLibOPDSEntry implements OPDSEntry {

    private final String id;
    private final Res title;
    private final List<OPDSLink> links;
    private final Instant updated;
    private final List<OPDSContent> content;
    private final List<OPDSAuthor> authors;

    public ExtLibOPDSEntry(SyndEntry syndEntry) {
        id = syndEntry.getUri();
        title = new Res("first.value", syndEntry.getTitle());
        links = syndEntry.getLinks().stream().
                map(LinkMapper::mapLink).filter(Objects::nonNull).collect(Collectors.toList());
        updated = syndEntry.getUpdatedDate() == null ? null : syndEntry.getUpdatedDate().toInstant();
        content = syndEntry.getContents().stream().
                map(sc -> new OPDSContent(sc.getType(), sc.getValue(), null)).collect(Collectors.toList());

        this.authors = syndEntry.getAuthors().stream().
                map(person -> new OPDSAuthorImpl(person.getName(), "")).collect(Collectors.toList());
    }

    public ExtLibOPDSEntry(OPDSEntry entry, String linkPrefix) {
        this.id = entry.getId();
        this.title = entry.getTitle();
        this.links = entry.getLinks().stream().
                map(link -> new OPDSLink(linkPrefix + link.getHref(), link.getRel(), link.getType()))
                .collect(Collectors.toList());
        updated = entry.getUpdated();
        content = entry.getContent();
        authors = entry.getAuthors();
    }

    @Override
    public Instant getUpdated() {
        return updated;
    }

    @Override
    public List<OPDSContent> getContent() {
        return content;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Res getTitle() {
        return title;
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
