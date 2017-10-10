package com.patex.opds.converters;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.Book;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.utils.LinkUtils;

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
                map(Book::getCreated).max(Instant::compareTo).map(Date::from).orElse(null);
        List<String> descrLines = author.getDescr() == null ? null : Arrays.asList(author.getDescr().split("\n"));
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date, "Книги автора " + author.getName(), descrLines));

        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), date, author.getName() + " Книги по алфавиту",
                author.getBooks().size() + " Книг",
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));

        List<Sequence> sequences = author.getSequences();

        int bookCount = sequences.stream().mapToInt(sequence -> sequence.getBookSequences().size()).sum();
        Date sequencesDate=sequences.stream().map(Sequence::getBookSequences).flatMap(List::stream).
                map(BookSequence::getBook).
                map(Book::getCreated).max(Instant::compareTo).map(Date::from).orElse(null);
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), sequencesDate,
                author.getName() + "Книги по сериям",
                sequences.size() + " Серий. " + bookCount + " Книг",
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));

        Date sequencelessDate=author.getBooksNoSequence().stream().map(AuthorBook::getBook).
                map(Book::getCreated).max(Instant::compareTo).map(Date::from).orElse(null);
        entries0.add(new OPDSEntryImpl("authorsequenceless" + author.getId(), sequencelessDate, author.getName() + "Книги вне серий",
                author.getBooksNoSequence().size() + " Книг",
                LinkUtils.makeURL("opds", "authorsequenceless", author.getId())));

        this.entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntryI> getEntries() {
        return entries;
    }


}
