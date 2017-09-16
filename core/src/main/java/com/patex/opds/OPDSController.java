package com.patex.opds;

import com.patex.entities.Author;
import com.patex.entities.AuthorBook;
import com.patex.entities.BookSequence;
import com.patex.entities.Sequence;
import com.patex.service.AuthorService;
import com.patex.service.SequenceService;
import com.patex.utils.LinkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Controller
@RequestMapping(OPDSController.PREFIX)
public class OPDSController {

    public static final String PREFIX = "opds";
    private static final String AUTHORSINDEX = "authorsindex";
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    private static Logger log = LoggerFactory.getLogger(OPDSController.class);

    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private LatestURIComponent latestURIComponent;

    private final List<OPDSEntryI> rootEntries = new ArrayList<>();


    @PostConstruct
    public void setUp() {
        rootEntries.add(new OPDSEntryImpl("root:latest", "Последнее",
                new OPDSLink(LinkUtils.makeURL(PREFIX, "latest"), OPDSLink.OPDS_CATALOG)));
        rootEntries.add(new OPDSEntryImpl("root:authors", "По Авторам",
                new OPDSLink(LinkUtils.makeURL(PREFIX, AUTHORSINDEX), OPDSLink.OPDS_CATALOG)));
    }

    public void addRoot(OPDSEntryI entry) {
        rootEntries.add(entry);
    }

    @RequestMapping(produces = "application/atom+xml")
    public ModelAndView getMain() {
        return createMav("Zombie Catalog", rootEntries);
    }

    @RequestMapping(value = AUTHORSINDEX, produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex() {
        return getAuthorsIndex("");
    }

    @SaveLatest
    @RequestMapping(value = AUTHORSINDEX + "/{start}", produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex(@PathVariable(value = "start") String start) {
        return createMav("", authorService.getAuthorsCount(start), aggrResults -> {
            List<OPDSEntryI> entries = new ArrayList<>();
            List<OPDSEntryI> authors = aggrResults.stream().
                    filter(aggrResult -> aggrResult.getResult() <= EXPAND_FOR_AUTHORS_COUNT).
                    flatMap(aggrResult -> authorService.findByName(aggrResult.getId()).stream()).
                    map(AuthorEntry::new).
                    sorted(Comparator.comparing(OPDSEntryI::getTitle)).
                    collect(Collectors.toList());
            entries.addAll(authors);

            List<OPDSEntryI> serachEntries = aggrResults.stream().
                    filter(aggrResult -> aggrResult.getResult() > EXPAND_FOR_AUTHORS_COUNT && aggrResults.size() != 1).
                    map(aggr ->
                            createEntry(aggr.getId(), aggr.getId(),
                                    LinkUtils.makeURL("opds", AUTHORSINDEX, aggr.getId()))).
                    collect(Collectors.toList());
            entries.addAll(serachEntries);
            return entries;
        });
    }

    @SaveLatest
    @RequestMapping(value = "author/{id}", produces = "application/atom+xml")
    public ModelAndView getAuthor(@PathVariable(value = "id") long id) {
        return createMav("", authorService.getAuthors(id), author ->
                new ExpandedAuthorEntry(author).getEntries()
        );
    }

    @SaveLatest
    @RequestMapping(value = "author/{id}/alphabet", produces = "application/atom+xml")
    public ModelAndView getAuthorBookAlphabet(@PathVariable(value = "id") long id) {
        Author bookAuthor = authorService.getAuthors(id);
        return createMav("Книги по алфавиту " + bookAuthor.getName(), bookAuthor, author ->
                author.getBooks().stream().
                        map(AuthorBook::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @SaveLatest
    @RequestMapping(value = "authorsequenceless/{id}", produces = "application/atom+xml")
    public ModelAndView getAuthorBookNoSequence(@PathVariable(value = "id") long id) {
        Author bookAuthor = authorService.getAuthors(id);
        return createMav("Книги по алфавиту " + bookAuthor.getName(), bookAuthor, author ->
                author.getBooksNoSequence().stream().
                        map(AuthorBook::getBook).
                        filter(book -> !book.isDuplicate()).
                        map(BookEntry::new).
                        collect(Collectors.toList()));
    }

    @SaveLatest
    @RequestMapping(value = "sequence/{id}", produces = "application/atom+xml")
    public ModelAndView getBookBySequence(@PathVariable(value = "id") long id) {
        Sequence sequence = sequenceService.getSequence(id);
        return createMav("Книги в серии " + sequence.getName(), sequence, seq ->
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
        return createMav("Книжные сериии " + author.getName(), author,
                a -> a.getSequencesStream().map(SequenceEntry::new).
                        collect(Collectors.toList()));
    }


    @RequestMapping(value = "latest")
    public ModelAndView getLatest(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
        return latestURIComponent.getLatestForCurrentUser();
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, Exception ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error");
        return createMav("Error", Collections.singletonList(new OPDSEntryI() {
            @Override
            public String getId() {
                return "Error";
            }

            @Override
            public String getTitle() {
                return ex.getMessage();
            }

            @Override
            public List<OPDSLink> getLinks() {
                return Collections.emptyList();
            }

            @Override
            public Optional<List<String>> getContent() {
                return Optional.of(Collections.singletonList(ex.getMessage()));
            }
        }));
    }

    public static OPDSEntryI createEntry(String id, String title, String... hrefs) {
        List<OPDSLink> links = Arrays.stream(hrefs).
                map(s -> new OPDSLink(s, OPDSLink.OPDS_CATALOG)).collect(Collectors.toList());
        return new OPDSEntryI() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public List<OPDSLink> getLinks() {
                return links;
            }
        };
    }


    public static <E> ModelAndView createMav(String title, E e, Function<E, List<OPDSEntryI>> func) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        if (e != null) {
            List<OPDSEntryI> entries = func.apply(e);
            mav.addObject(OpdsView.ENTRIES, entries);
        } else {
            log.warn("empty obj:" + title);
        }
        mav.addObject(OpdsView.TITLE, title);
        return mav;
    }

    public static ModelAndView createMav(String title, List<OPDSEntryI> entries) {
        return createMav(title, entries, e -> e);
    }
}
