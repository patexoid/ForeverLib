package com.patex.opds.converters;

import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.opds.*;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 23.05.2017.
 */
public class BookEntry implements OPDSEntry {

    private final String id;
    private final Instant updated;
    private final Res title;
    private final List<OPDSAuthor> authors;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;

    public BookEntry(Book book) {
        id = "book:" + book.getId();
        updated = book.getCreated();
        title = new Res("first.value", book.getTitle());
        authors = book.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(OPDSAuthorImpl::new).
                collect(Collectors.toList());
        // TODO entry.setCategories();

        List<OPDSContent> content0 = new ArrayList<>();
        content0.add(new OPDSContent(book.getDescr()));
        content0.addAll(book.getSequences().stream().
                map(bs -> " Серия:" + bs.getSequence().getName() + " #" + bs.getSeqOrder()).
                map(OPDSContent::new).
                collect(Collectors.toList()));
        content = Collections.unmodifiableList(content0);
        OPDSLink downloadLink = new OPDSLink(LinkUtils.makeURL("/book/loadFile", book.getId()), OPDSLink.FB2_ZIP);

        String imageUrl = LinkUtils.makeURL("/book/cover", book.getId());
        OPDSLink imageLink1 =
                new OPDSLink(imageUrl, "http://opds-spec.org/image", book.getFileResource().getType());
        OPDSLink imageLink2 =
                new OPDSLink(imageUrl, "x-stanza-cover-image", book.getFileResource().getType());
        OPDSLink imageLink3 =
                new OPDSLink(imageUrl, "http://opds-spec.org/thumbnail", book.getFileResource().getType());
        OPDSLink imageLink4 =
                new OPDSLink(imageUrl, "x-stanza-cover-image-thumbnail", book.getFileResource().getType());

        links = Arrays.asList(downloadLink, imageLink1, imageLink2, imageLink3, imageLink4);


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

    public List<OPDSAuthor> getAuthors() {
        return authors;
    }

    @Override
    public List<OPDSContent> getContent() {
        return content;
    }


    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
