package com.patex.zombie.opds.model.converters;

import com.patex.model.Book;
import com.patex.model.Sequence;
import com.patex.model.SequenceBook;
import com.patex.zombie.opds.model.OPDSAuthor;
import com.patex.zombie.opds.model.OPDSContent;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.patex.zombie.opds.utils.LinkUtils;
import com.patex.zombie.core.utils.Res;

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
                singletonList(new OPDSContent("Количество книг в серии: " + sequence.getBooks().size()));
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("/opds/sequence", sequence.getId()), OPDSLink.OPDS_CATALOG));
        date = sequence.getBooks().stream().map(SequenceBook::getBook).map(Book::getCreated).
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
