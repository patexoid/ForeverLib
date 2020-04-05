package com.patex.zombie.opds.model.converter;

import com.patex.zombie.LinkUtils;
import com.patex.zombie.model.Author;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.Res;
import com.patex.zombie.model.Sequence;
import com.patex.zombie.model.SequenceBook;
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

    public ExpandedAuthorEntries(Author author) {
        List<OPDSEntry> entries0 = new ArrayList<>();
        Instant date = author.getUpdated();
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date,
                new Res("opds.author.books", author.getName()), author.getDescr()));

        int bookCount = (int) author.getBooks().stream().filter(Book::isPrimary).count();
        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), date,
                new Res("opds.author.books.alphabet", author.getName()),
                new ODPSContentRes("opds.book.count", bookCount),
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));

        List<Sequence> sequences = author.getSequences();

        int sequnceBookCount = (int) sequences.stream().
                flatMap(s -> s.getBooks().stream()).
                map(SequenceBook::getBook).
                filter(Book::isPrimary).
                count();
        Instant sequencesDate = sequences.stream().
                map(Sequence::getBooks).
                flatMap(List::stream).
                map(SequenceBook::getBook).
                filter(Book::isPrimary).
                map(Book::getCreated).max(Instant::compareTo).orElse(Instant.now());
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), sequencesDate,
                new Res("opds.author.books.sequence", author.getName()),
                new ODPSContentRes("opds.book.and.series.count", sequences.size(), sequnceBookCount),
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));

        Instant sequencelessDate = author.getBooksNoSequence().stream().
                filter(Book::isPrimary).
                map(Book::getCreated).
                max(Instant::compareTo).orElse(Instant.now());
        int booknoSequenceCount = (int) author.getBooksNoSequence().stream().
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
