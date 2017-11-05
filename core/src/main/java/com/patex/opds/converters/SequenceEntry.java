package com.patex.opds.converters;

import com.patex.entities.Book;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.opds.OPDSContent;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by Alexey on 23.05.2017.
 */
public class SequenceEntry implements OPDSEntryI {

    private final String id;
    private final Res title;
    private final List<OPDSContent> content;
    private final List<OPDSLink> links;
    private final Date date;

    public SequenceEntry(Sequence sequence) {

        id = "sequence:" + sequence.getId();
        title = new Res("opds.first.value",sequence.getName());
        content = Collections.
                singletonList(new OPDSContent("Количество книг в серии: " + sequence.getBookSequences().size()));
        links = Collections.singletonList(
                new OPDSLink(LinkUtils.makeURL("/opds/sequence", sequence.getId()), OPDSLink.OPDS_CATALOG));
        date = sequence.getBookSequences().stream().map(BookSequence::getBook).map(Book::getCreated).
                max(Instant::compareTo).map(Date::from).orElse(null);
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
        return Optional.of(content);
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
