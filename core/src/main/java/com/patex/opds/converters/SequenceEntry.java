package com.patex.opds.converters;

import com.patex.entities.Book;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.opds.OPDSContent;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexey on 23.05.2017.
 */
public class SequenceEntry implements OPDSEntry {

    private final String id;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final Instant date;

    public SequenceEntry(Sequence sequence) {

        id = "sequence:" + sequence.getId();
        title = new Res("first.value", sequence.getName());
        content = Collections.
                singletonList(new OPDSContent("Количество книг в серии: " + sequence.getBookSequences().size()));
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("/opds/sequence", sequence.getId()), OPDSLink.OPDS_CATALOG));
        date = sequence.getBookSequences().stream().map(BookSequence::getBook).map(Book::getCreated).
                max(Instant::compareTo).orElse(Instant.now());
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
    public List<OPDSContent> getContent() {
        return content;
    }

    @Override
    public List<OPDSLink> getLinks() {
        return links;
    }

    @Override
    public Instant getUpdated() {
        return date;
    }

    @Override
    public List<OPDSAuthor> getAuthors() {
        return Collections.emptyList();
    }
}
