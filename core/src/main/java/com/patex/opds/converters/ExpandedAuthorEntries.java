package com.patex.opds.converters;

import com.patex.entities.*;
import com.patex.opds.ODPSContentRes;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Alexey on 23.05.2017.
 */
public class ExpandedAuthorEntries {

    private final List<OPDSEntry> entries;

    public ExpandedAuthorEntries(Author author) {
        List<OPDSEntry> entries0 = new ArrayList<>();
        Date date = author.getBooks().stream().map(AuthorBook::getBook).
                filter(Book::isPrimary).
                map(Book::getCreated).
                max(Instant::compareTo).
                map(Date::from).orElse(null);
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date,
                new Res("opds.author.books", author.getName()), author.getDescr()));

        int bookCount = (int) author.getBooks().stream().map(AuthorBook::getBook).filter(Book::isPrimary).count();
        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), date,
                new Res("opds.author.books.alphabet", author.getName()),
                new ODPSContentRes("opds.book.count", bookCount),
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));

        List<Sequence> sequences = author.getSequences();

        int sequnceBookCount = (int) sequences.stream().
                flatMap(s -> s.getBookSequences().stream()).
                map(BookSequence::getBook).
                filter(Book::isPrimary).
                count();
        Date sequencesDate = sequences.stream().
                map(Sequence::getBookSequences).
                flatMap(List::stream).
                map(BookSequence::getBook).
                filter(Book::isPrimary).
                map(Book::getCreated).max(Instant::compareTo).map(Date::from).orElse(null);
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), sequencesDate,
                new Res("opds.author.books.sequence", author.getName()),
                new ODPSContentRes("opds.book.and.series.count", sequences.size(), sequnceBookCount),
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));

        Date sequencelessDate = author.getBooksNoSequence().stream().
                map(AuthorBook::getBook).
                filter(Book::isPrimary).
                map(Book::getCreated).
                max(Instant::compareTo).
                map(Date::from).orElse(null);
        int booknoSequenceCount = (int) author.getBooksNoSequence().stream().
                map(AuthorBook::getBook).
                filter(Book::isPrimary).
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
