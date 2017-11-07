package com.patex.opds.converters;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.opds.OPDSContent;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 07.05.2017.
 */
public class AuthorEntry implements OPDSEntryI {

    private final String id;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final Date date;


    public AuthorEntry(Author author) {
        id = "author:" + author.getId();
        title = new Res("opds.first.value",author.getName());
        String descr = author.getDescr();
        if (author.getDescr() != null) {
            content = Arrays.stream(descr.split("\n")).map(OPDSContent::new).collect(Collectors.toList());
        } else {
            content = null;
        }
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("opds", "author", author.getId()), OPDSLink.OPDS_CATALOG)
        );
        date = author.getBooks().stream().map(AuthorBook::getBook).
                map(Book::getCreated).max(Instant::compareTo).map(Date::from).orElse(null);

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
    public Optional<List<OPDSContent>> getContent() {
        return Optional.ofNullable(content);
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }

    @Override
    public Date getUpdated() {
        return date;
    }
}
