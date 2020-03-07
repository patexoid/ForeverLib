package com.patex.zombie.opds.model.converters;


import com.patex.model.Author;
import com.patex.model.Book;
import com.patex.model.Sequence;
import com.patex.model.SequenceBook;
import com.patex.zombie.opds.model.ODPSContentRes;
import com.patex.opds.OPDSEntry;
import com.patex.utils.LinkUtils;

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
        entries0.add(OPDSEntry.builder("author:" + author.getId(), "opds.author.books", author.getName()).
                withUpdated(date).
                addContent(author.getDescr()).
                build());

        int bookCount = (int) author.getBooks().stream().filter(Book::isPrimary).count();
        entries0.add(OPDSEntry.builder("author_alphabet" + author.getId(), "opds.author.books.alphabet", author.getName()).
                withUpdated(date).
                addContent(new ODPSContentRes("opds.book.count", bookCount)).
                addLink(LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")).
                build());

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

        entries0.add(OPDSEntry.builder("authorsequences" + author.getId(), "opds.author.books.sequence", author.getName()).
                withUpdated(sequencesDate).
                addContent(new ODPSContentRes("opds.book.and.series.count", sequences.size(), sequnceBookCount)).
                addLink(LinkUtils.makeURL("opds", "authorsequences", author.getId())).
                build());

        Instant sequencelessDate = author.getBooksNoSequence().stream().
                filter(Book::isPrimary).
                map(Book::getCreated).
                max(Instant::compareTo).orElse(Instant.now());
        int booknoSequenceCount = (int) author.getBooksNoSequence().stream().
                filter(Book::isPrimary).
                count();

        entries0.add(OPDSEntry.builder("authorsequenceless" + author.getId(), "opds.author.books.sequenceless", author.getName()).
                withUpdated(sequencelessDate).
                addContent(new ODPSContentRes("opds.book.count", booknoSequenceCount)).
                addLink(LinkUtils.makeURL("opds", "authorsequenceless", author.getId())).
                build());
        entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntry> getEntries() {
        return entries;
    }


}
