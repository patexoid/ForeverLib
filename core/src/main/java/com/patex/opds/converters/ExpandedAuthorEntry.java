package com.patex.opds.converters;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.opds.ODPSContentRes;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Alexey on 23.05.2017.
 */
public class ExpandedAuthorEntry {

    private final List<OPDSEntryI> entries;

    public ExpandedAuthorEntry(Author author) {
        List<OPDSEntryImpl> entries0 = new ArrayList<>();
        Date date = author.getBooks().stream().map(AuthorBook::getBook).
                filter(Book::isPrimary).
                map(Book::getCreated).
                max(Instant::compareTo).
                map(Date::from).orElse(null);
        List<String> descrLines = author.getDescr() == null ? null : Arrays.asList(author.getDescr().split("\n"));
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date,
                new Res("opds.author.books", author.getName()),
                descrLines));

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

        this.entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntryI> getEntries() {
        return entries;
    }


}
