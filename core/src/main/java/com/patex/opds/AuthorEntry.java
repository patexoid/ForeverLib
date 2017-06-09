package com.patex.opds;

import com.patex.entities.Author;
import com.patex.utils.LinkUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 07.05.2017.
 */
public class AuthorEntry implements OPDSEntryI {

    private final String id;
    private final String title;
    private final List<String> content;
    private final List<OPDSLink> links;


    public AuthorEntry(Author author) {
        id = "author:" + author.getId();
        title = author.getName();
        String descr = author.getDescr();
        if (author.getDescr() != null) {
            content = Arrays.stream(descr.split("\n")).collect(Collectors.toList());
        } else {
            content = null;
        }
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("opds", "author", author.getId()), OPDSLink.OPDS_CATALOG)
        );
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Optional<List<String>> getContent() {
        return Optional.of(content);
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
