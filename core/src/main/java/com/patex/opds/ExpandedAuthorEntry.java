package com.patex.opds;

import com.patex.entities.Author;
import com.patex.utils.LinkUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Alexey on 23.05.2017.
 */
public class ExpandedAuthorEntry {

    private final List<OPDSEntryI> entries;

    public ExpandedAuthorEntry(Author author) {
        List<OPDSEntryImpl> entries0 = new ArrayList<>();
        List<String> descrLines = author.getDescr() == null ? null : Arrays.asList(author.getDescr().split("\n"));
        entries0.add(new OPDSEntryImpl("author" + author.getId(), null, "Книги автора " + author.getName(), descrLines));
        entries0.add(new OPDSEntryImpl("author_alphabet" + author.getId(), null, author.getName() + " Книги по алфавиту", null,
                LinkUtils.makeURL("opds", "author", author.getId(), "alphabet")));
        entries0.add(new OPDSEntryImpl("authorsequences" + author.getId(), null, author.getName() + "Книги по сериям", null,
                LinkUtils.makeURL("opds", "authorsequences", author.getId())));
        entries0.add(new OPDSEntryImpl("authorsequenceless" + author.getId(), null, author.getName() + "Книги вне серий", null,
                LinkUtils.makeURL("opds", "authorsequenceless", author.getId())));

        this.entries = Collections.unmodifiableList(entries0);
    }

    public List<OPDSEntryI> getEntries() {
        return entries;
    }


}
