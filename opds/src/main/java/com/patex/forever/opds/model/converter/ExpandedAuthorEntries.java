package com.patex.forever.opds.model.converter;

import com.patex.forever.LinkUtils;
import com.patex.forever.model.Author;
import com.patex.forever.model.AuthorDescription;
import com.patex.forever.model.Book;
import com.patex.forever.model.Res;
import com.patex.forever.model.Sequence;
import com.patex.forever.model.SequenceBook;
import com.patex.forever.opds.model.ODPSContentRes;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.model.OPDSEntryImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by Alexey on 23.05.2017.
 */
public class ExpandedAuthorEntries {

    private final List<OPDSEntry> entries;

    public ExpandedAuthorEntries(AuthorDescription author) {
        List<OPDSEntry> entries0 = new ArrayList<>();
        Instant date = author.getUpdated();
        entries0.add(new OPDSEntryImpl("author" + author.getId(), date,
                new Res("opds.author.books", author.getName()), author.getDescr()));

        int bookCount =  author.getBookCount();
        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), date,
                new Res("opds.author.books.alphabet", author.getName()),
                new ODPSContentRes("opds.book.count", bookCount),
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));


        int sequnceBookCount = author.getSequenceBookCount();
        Instant sequencesDate = author.getSequenceUpdated();
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), sequencesDate,
                new Res("opds.author.books.sequence", author.getName()),
                new ODPSContentRes("opds.book.and.series.count", author.getSequenceCount(), sequnceBookCount),
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));

        Instant sequencelessDate = author.getNoSequenceUpdated();
        int bookNoSequenceCount = (int) author.getNoSequenceBookCount();

        entries0.add(new OPDSEntryImpl("authorsequenceless" + author.getId(), sequencelessDate,
                new Res("opds.author.books.sequenceless", author.getName()),
                new ODPSContentRes("opds.book.count", bookNoSequenceCount),
                LinkUtils.makeURL("opds", "authorsequenceless", author.getId())));
        entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntry> getEntries() {
        return entries;
    }


}
