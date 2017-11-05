package com.patex.opds.converters;

import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.opds.OPDSContent;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Alexey on 23.05.2017.
 */
public class BookEntry implements OPDSEntryI {

    private final String id;
    private final Date updated;
    private final Res title;
    private final List<OPDSAuthor> authors;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;

    public BookEntry(Book book) {
        id = "book:" + book.getId();
        updated = Date.from(book.getCreated());
        title = new Res("opds.first.value",book.getTitle());
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
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("/book/loadFile", book.getId()), OPDSLink.FB2_ZIP));
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

    public Optional<List<OPDSAuthor>> getAuthors() {
        return Optional.ofNullable(authors);
    }

    @Override
    public Optional<List<OPDSContent>> getContent() {
        return Optional.ofNullable(content);
    }


    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }
}
