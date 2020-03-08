package com.patex.zombie.opds.model.converter;

import com.patex.entities.AuthorEntity;
import com.patex.entities.AuthorBookEntity;
import com.patex.entities.BookEntity;
import com.patex.entities.BookSequenceEntity;
import com.patex.entities.SequenceEntity;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;
import com.patex.zombie.opds.model.ODPSContentRes;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSEntryImpl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexey on 23.05.2017.
 */
public class ExpandedAuthorEntries {

    private final List<OPDSEntry> entries;

    public ExpandedAuthorEntries(AuthorEntity author) {
        List<OPDSEntry> entries0 = new ArrayList<>();
        Instant date = author.getUpdated();
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date,
                new Res("opds.author.books", author.getName()), author.getDescr()));

        int bookCount = (int) author.getBooks().stream().map(AuthorBookEntity::getBook).filter(BookEntity::isPrimary).count();
        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), date,
                new Res("opds.author.books.alphabet", author.getName()),
                new ODPSContentRes("opds.book.count", bookCount),
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));

        List<SequenceEntity> sequences = author.getSequences();

        int sequnceBookCount = (int) sequences.stream().
                flatMap(s -> s.getBookSequences().stream()).
                map(BookSequenceEntity::getBook).
                filter(BookEntity::isPrimary).
                count();
        Instant sequencesDate = sequences.stream().
                map(SequenceEntity::getBookSequences).
                flatMap(List::stream).
                map(BookSequenceEntity::getBook).
                filter(BookEntity::isPrimary).
                map(BookEntity::getCreated).max(Instant::compareTo).orElse(Instant.now());
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), sequencesDate,
                new Res("opds.author.books.sequence", author.getName()),
                new ODPSContentRes("opds.book.and.series.count", sequences.size(), sequnceBookCount),
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));

        Instant sequencelessDate = author.getBooksNoSequence().stream().
                map(AuthorBookEntity::getBook).
                filter(BookEntity::isPrimary).
                map(BookEntity::getCreated).
                max(Instant::compareTo).orElse(Instant.now());
        int booknoSequenceCount = (int) author.getBooksNoSequence().stream().
                map(AuthorBookEntity::getBook).
                filter(BookEntity::isPrimary).
                count();

        entries0.add(new OPDSEntryImpl("authorsequenceless" + author.getId(), sequencelessDate,
                new Res("opds.author.books.sequenceless", author.getName()),
                new ODPSContentRes("opds.book.count", booknoSequenceCount),
                LinkUtils.makeURL("opds", "authorsequenceless", author.getId())));
        entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntry> getEntries() {
        return entries;
    }


}
