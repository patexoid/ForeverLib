package com.patex.controllers;

import com.patex.entities.AggrResult;
import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.opds.OPDSMetadata;
import com.patex.opds.OpdsView;
import com.patex.opds.RootProvider;
import com.patex.opds.converters.AuthorEntry;
import com.patex.opds.converters.BookEntry;
import com.patex.opds.converters.ExpandedAuthorEntry;
import com.patex.opds.converters.OPDSEntryI;
import com.patex.opds.converters.OPDSEntryImpl;
import com.patex.opds.converters.OPDSLink;
import com.patex.opds.converters.SequenceEntry;
import com.patex.opds.latest.LatestURIComponent;
import com.patex.opds.latest.SaveLatest;
import com.patex.service.AuthorService;
import com.patex.service.SequenceService;
import com.patex.utils.LinkUtils;
import com.patex.utils.Res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Controller
@RequestMapping(OPDSController.PREFIX)
public class OPDSController {

    public static final String PREFIX = "opds";
    public static final String APPLICATION_ATOM_XML = "application/atom+xml;charset=UTF-8";
    private static final String AUTHORSINDEX = "authorsindex";
    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;
    private static Logger log = LoggerFactory.getLogger(OPDSController.class);
    private final List<RootProvider> rootEntriesProvider = new ArrayList<>();
    @Autowired
    private AuthorService authorService;
    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private LatestURIComponent latestURIComponent;

    public static <E> ModelAndView createMav(Res title, E e, Function<E, List<OPDSEntryI>> func, Date updated) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        if (e != null) {
            List<OPDSEntryI> entries = func.apply(e);
            mav.addObject(OpdsView.ENTRIES, entries);
            if (updated == null) {
                updated = entries.stream().map(OPDSEntryI::getUpdated).filter(Objects::nonNull).
                        max(Date::compareTo).orElse(null);
            }
        } else {
            log.warn("empty obj:" + title);
        }
        mav.addObject(OpdsView.OPDS_METADATA, new OPDSMetadata(title, title.getKey(), updated));
        return mav;
    }

    public static <E> ModelAndView createMav(Res title, E e, Function<E, List<OPDSEntryI>> func) {
        return createMav(title, e, func, null);
    }

    public static ModelAndView createMav(Res title, List<OPDSEntryI> entries) {
        return createMav(title, entries, e -> e);
    }

    public static ModelAndView createMav(Res title, List<OPDSEntryI> entries, Date updated) {
        return createMav(title, entries, e -> e, updated);
    }

    public void addRootPrivider(RootProvider provider) {
        rootEntriesProvider.add(provider);
    }

    @RequestMapping(produces = APPLICATION_ATOM_XML)
    public ModelAndView getMain() {
        List<OPDSEntryI> rootEntries = new ArrayList<>();
        rootEntries.add(new OPDSEntryImpl("root:latest", new Res("opds.latest"),
                new OPDSLink(LinkUtils.makeURL(PREFIX, "opds.latest"), OPDSLink.OPDS_CATALOG)));
        rootEntries.add(new OPDSEntryImpl("root:authors", new Res("opds.all.authors"),
                new OPDSLink(LinkUtils.makeURL(PREFIX, AUTHORSINDEX), OPDSLink.OPDS_CATALOG)));
        for (RootProvider rootProvider : rootEntriesProvider) {
            rootEntries.addAll(rootProvider.getRoot());
        }
        return createMav(new Res("opds.catalog"), rootEntries);
    }

    @RequestMapping(value = AUTHORSINDEX, produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorsIndex() {
        return getAuthorsIndex("");
    }

    @SaveLatest
    @RequestMapping(value = AUTHORSINDEX + "/{start}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorsIndex(@PathVariable(value = "start") String start) {
        return createMav(new Res("opds.all.authors"), authorService.getAuthorsCount(start),
                aggrResults -> aggrResults.stream().
                flatMap(this::expandAggrResult).
                sorted(Comparator.comparing(OPDSEntryI::getTitle)).
                collect(Collectors.toList()));
    }


    private Stream<OPDSEntryI> expandAggrResult(AggrResult aggr) {
        if (aggr.getResult() >= EXPAND_FOR_AUTHORS_COUNT) {
            String link = LinkUtils.makeURL("opds", AUTHORSINDEX, LinkUtils.encode(aggr.getId()));
            Res title = new Res("opds.first.value", aggr.getId());
            return Stream.of(new OPDSEntryImpl(aggr.getId(), title, null, link));
        } else {
            return authorService.findByName(aggr.getId()).stream().map(AuthorEntry::new);
        }
    }

    @SaveLatest
    @RequestMapping(value = "author/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthor(@PathVariable(value = "id") long id) {
        Author authors = authorService.getAuthors(id);
        return createMav(new Res("opds.author.books", authors.getName()), authors, author ->
                new ExpandedAuthorEntry(author).getEntries()
        );
    }

    @SaveLatest
    @RequestMapping(value = "author/{id}/alphabet", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorBookAlphabet(@PathVariable(value = "id") long id) {
        Author bookAuthor = authorService.getAuthors(id);
        return createMav(new Res("opds.author.books.alphabet", bookAuthor.getName()), bookAuthor, author ->
                author.getBooks().stream().
                        map(AuthorBook::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @SaveLatest
    @RequestMapping(value = "authorsequenceless/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorBookNoSequence(@PathVariable(value = "id") long id) {
        Author bookAuthor = authorService.getAuthors(id);
        return createMav(new Res("opds.author.books.sequenceless", bookAuthor.getName()), bookAuthor, author ->
                author.getBooksNoSequence().stream().
                        map(AuthorBook::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @SaveLatest
    @RequestMapping(value = "sequence/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getBookBySequence(@PathVariable(value = "id") long id) {
        Sequence sequence = sequenceService.getSequence(id);
        return createMav(new Res("opds.author.books.sequence", sequence.getName()), sequence, seq ->
                seq.getBookSequences().stream().
                        sorted(Comparator.comparing(BookSequence::getSeqOrder)).
                        map(BookSequence::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList())
        );
    }

    @SaveLatest
    @RequestMapping(value = "authorsequences/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorSequences(@PathVariable(value = "id") long id) {
        Author author = authorService.getAuthors(id);
        return createMav(new Res("opds.author.sequence", author.getName()), author,
                a -> a.getSequencesStream().map(SequenceEntry::new).
                        collect(Collectors.toList()));
    }

    @RequestMapping(value = "latest")
    public ModelAndView getLatest(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
        response.setHeader("Content-Type",APPLICATION_ATOM_XML);
        return latestURIComponent.getLatestForCurrentUser();
    }
}
