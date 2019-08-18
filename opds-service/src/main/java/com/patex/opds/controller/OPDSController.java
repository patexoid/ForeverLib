package com.patex.opds.controller;

import com.patex.model.Author;
import com.patex.model.Sequence;
import com.patex.model.SequenceBook;
import com.patex.opds.model.OPDSEntry;
import com.patex.opds.model.OPDSLink;
import com.patex.opds.model.OPDSMetadata;
import com.patex.opds.model.OpdsView;
import com.patex.opds.model.RootProvider;
import com.patex.opds.model.converters.BookEntry;
import com.patex.opds.model.converters.ExpandedAuthorEntries;
import com.patex.opds.model.converters.SequenceEntry;
import com.patex.opds.service.OpdsService;
import com.patex.opds.utils.LinkUtils;
import com.patex.utils.Res;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping(OPDSController.PREFIX)
@RequiredArgsConstructor
public class OPDSController {

    static final String PREFIX = "opds";
    static final String APPLICATION_ATOM_XML = "application/atom+xml;charset=UTF-8";
    private static final String AUTHORSINDEX = "authorsindex";
    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;
    private static final Logger log = LoggerFactory.getLogger(OPDSController.class);
    private final List<RootProvider> rootEntriesProvider = new ArrayList<>();

    private final OpdsService service;

    private static <E> ModelAndView createMav(Res title, E e, Function<E, List<OPDSEntry>> func, Instant updated) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        if (e != null) {
            List<OPDSEntry> entries = func.apply(e);
            mav.addObject(OpdsView.ENTRIES, entries);
            if (updated == null) {
                updated = entries.stream().map(OPDSEntry::getUpdated).filter(Objects::nonNull).
                        max(Instant::compareTo).orElse(Instant.now());
            }
        } else {
            log.warn("empty obj:" + title);
        }
        mav.addObject(OpdsView.OPDS_METADATA, new OPDSMetadata(title, title.getKey(), updated));
        return mav;
    }

    private static <E> ModelAndView createMav(Res title, E e, Function<E, List<OPDSEntry>> func) {
        return createMav(title, e, func, null);
    }

    static ModelAndView createMav(Res title, List<OPDSEntry> entries) {
        return createMav(title, entries, e -> e);
    }

    public static ModelAndView createMav(Res title, List<OPDSEntry> entries, Instant updated) {
        return createMav(title, entries, e -> e, updated);
    }

    public void addRootPrivider(RootProvider provider) {
        rootEntriesProvider.add(provider);
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public ModelAndView getMain() {
        List<OPDSEntry> rootEntries = new ArrayList<>();
        rootEntries.add(OPDSEntry.builder("root:latest", "opds.latest").
                addLink(LinkUtils.makeURL(PREFIX, "latest"), OPDSLink.OPDS_CATALOG).
                build());
        rootEntries.add(OPDSEntry.builder("root:newBooks", "opds.newBooks").
                addLink(LinkUtils.makeURL(PREFIX, "newBooks"), OPDSLink.OPDS_CATALOG).
                build());
        rootEntries.add(OPDSEntry.builder("root:authors", "opds.all.authors").
                addLink(LinkUtils.makeURL(PREFIX, AUTHORSINDEX), OPDSLink.OPDS_CATALOG).
                build());
        rootEntries.addAll(
                rootEntriesProvider.stream().
                        map(RootProvider::getRoot).
                        flatMap(Collection::stream).
                        collect(Collectors.toList()));
        return createMav(new Res("opds.catalog"), rootEntries);
    }

    @RequestMapping(value = AUTHORSINDEX, produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorsIndex() {
        return getAuthorsIndex("");
    }

    @RequestMapping(value = AUTHORSINDEX + "/{start}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorsIndex(@PathVariable(value = "start") String start) {
        return createMav(new Res("opds.all.authors"), service.getAuthorsCount(start));
    }

    @RequestMapping(value = "author/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthor(@PathVariable(value = "id") long id) {
        Author authors = service.getAuthor(id);
        return createMav(new Res("opds.author.books", authors.getName()), authors, author ->
                new ExpandedAuthorEntries(author).getEntries()
        );
    }

    @RequestMapping(value = "author/{id}/alphabet", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorBookAlphabet(@PathVariable(value = "id") long id) {
        Author bookAuthor = service.getAuthor(id);
        return createMav(new Res("opds.author.books.alphabet", bookAuthor.getName()), bookAuthor, author ->
                author.getBooks().stream().
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @RequestMapping(value = "authorsequenceless/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorBookNoSequence(@PathVariable(value = "id") long id) {
        Author bookAuthor = service.getAuthor(id);
        return createMav(new Res("opds.author.books.sequenceless", bookAuthor.getName()), bookAuthor, author ->
                author.getBooksNoSequence().stream().
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @RequestMapping(value = "sequence/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getBookBySequence(@PathVariable(value = "id") long id) {
        Sequence sequence = service.getSequence(id);
        return createMav(new Res("opds.author.books.sequence", sequence.getName()), sequence, seq ->
                seq.getBooks().stream().
                        sorted(Comparator.comparing(SequenceBook::getSeqOrder)).
                        map(SequenceBook::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList())
        );
    }

    @RequestMapping(value = "authorsequences/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorSequences(@PathVariable(value = "id") long id) {
        Author author = service.getAuthor(id);
        return createMav(new Res("opds.author.sequence", author.getName()), author,
                a -> a.getSequences().stream().map(SequenceEntry::new).
                        collect(Collectors.toList()));
    }
}
