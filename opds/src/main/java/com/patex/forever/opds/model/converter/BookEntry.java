package com.patex.forever.opds.model.converter;

import com.patex.forever.LinkUtils;
import com.patex.forever.model.Book;
import com.patex.forever.model.FileResource;
import com.patex.forever.model.Res;
import com.patex.forever.opds.model.OPDSContent;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.model.OPDSLink;

import java.time.Instant;
import java.util.ArrayList;
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
        authors = book.getAuthors().stream().map(OPDSAuthorImpl::new).
                collect(Collectors.toList());
        // TODO entry.setCategories();

        List<OPDSContent> content0 = new ArrayList<>();
        content0.add(new OPDSContent(book.getDescr()));
        content0.addAll(book.getSequences().stream().
                map(bs -> " Серия:" + bs.getSequenceName() + " #" + bs.getSeqOrder()).
                map(OPDSContent::new).
                collect(Collectors.toList()));
        content = Collections.unmodifiableList(content0);
        OPDSLink downloadLink = new OPDSLink(LinkUtils.makeURL("/book/loadFile", book.getId()), OPDSLink.FB2_ZIP);

        links = new ArrayList<>();
        links.add(downloadLink);

        FileResource cover = book.getCover();
        if (cover != null) {
            String imageUrl = LinkUtils.makeURL("/book/cover", book.getId());
            links.add(
                    new OPDSLink(imageUrl, "http://opds-spec.org/image", cover.getType()));
            links.add(
                    new OPDSLink(imageUrl, "x-stanza-cover-image", cover.getType()));
            links.add(
                    new OPDSLink(imageUrl, "http://opds-spec.org/thumbnail", cover.getType()));
            links.add(
                    new OPDSLink(imageUrl, "x-stanza-cover-image-thumbnail", cover.getType()));
        }
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
