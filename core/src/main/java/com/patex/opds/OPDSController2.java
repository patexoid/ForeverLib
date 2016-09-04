package com.patex.opds;

import com.patex.entities.*;
import com.patex.service.AuthorService;
import com.patex.service.BookService;
import com.patex.service.SequenceService;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("opds")
public class OPDSController2 {

    private static Logger log = LoggerFactory.getLogger(OPDSController2.class);

    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;

    @Autowired
    AuthorService authorService;

    @Autowired
    BookService bookService;

    @Autowired
    SequenceService sequenceService;


    @RequestMapping(produces = "application/atom+xml")
    public ModelAndView getMain() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        mav.addObject(OpdsView.TITLE, "Zombie Catalog");
        List<Entry> entries = new ArrayList<>(4);
        entries.add(createEntry("root:authors", "По Авторам", "/opds/authorsindex"));
        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }

    private Entry createEntry(String id, String title, String... hrefs) {
        Entry authorEntry = new Entry();
        authorEntry.setId(id);
        authorEntry.setTitle(title);
        List<Link> links = new ArrayList<>();
        for (String href : hrefs) {
            Link link = new Link();
            link.setHref(href);
            link.setRel(null);
            link.setType("application/atom+xml;profile=opds-catalog");
            links.add(link);
        }
        authorEntry.setOtherLinks(links);
        return authorEntry;
    }

    @RequestMapping(value = "/authorsindex", produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex() {
        return getAuthorsIndex("");
    }

    @RequestMapping(value = "/authorsindex/{start}", produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex(@PathVariable(value = "start") String start) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        List<AggrResult> authorsCount = authorService.getAuthorsCount(start);

        List<Entry> entries = new ArrayList<>();

        List<Entry> authors = authorsCount.stream().
                filter(aggrResult -> aggrResult.getResult() < EXPAND_FOR_AUTHORS_COUNT).
                flatMap(aggrResult -> authorService.findByName(aggrResult.getId()).stream()).
                map(author ->
                        createEntry("" + author.getId(), author.getName(),
                                "/opds/author/" + author.getId(),
                                "/opds/authorsequences/" + author.getId(),
                                "/opds/authorsequenceless/" + author.getId())).
                sorted((o1, o2) -> o1.getTitle().compareTo(o2.getTitle())).
                collect(Collectors.toList());
        entries.addAll(authors);

        List<Entry> serachEntries = authorsCount.stream().
                filter(aggrResult -> aggrResult.getResult() > EXPAND_FOR_AUTHORS_COUNT && authorsCount.size() != 1).
                map(aggr -> createEntry(aggr.getId(), aggr.getId(), "/opds/authorsindex/" + aggr.getId())).
                collect(Collectors.toList());
        entries.addAll(serachEntries);

        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }

    @RequestMapping(value = "/author/{id}", produces = "application/atom+xml")
    public ModelAndView getAuthor(@PathVariable(value = "id") long id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        List<Entry> entries = new ArrayList<>();
        Author author = authorService.getAuthor(id);
        if (author == null) {
            return mav;
        }
        Entry entry = new Entry();
        entry.setTitle("Книги автора " + author.getName());
        Content content = new Content();
        content.setType("text/html");
        content.setValue(author.getDescr());
        entry.setContents(Collections.singletonList(content));
        entries.add(entry);
        entries.add(createEntry("" + author.getId(), author.getName() + "Книги по алфавиту",
                "/opds/author/" + author.getId() + "/alphabet"));
        entries.add(createEntry("" + author.getId(), author.getName() + "Книги по сериям",
                "/opds/authorsequences/" + author.getId()));
        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }


    @RequestMapping(value = "/author/{id}/alphabet", produces = "application/atom+xml")
    public ModelAndView getAuthorBookAlphabet(@PathVariable(value = "id") long id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        Author bookAuthor = authorService.getAuthor(id);
        if (bookAuthor == null) {
            return mav;
        }
        mav.addObject(OpdsView.TITLE, "Книги по алфавиту " + bookAuthor.getName());

        List<Entry> entries = bookAuthor.getBooks().stream().map(OPDSController2::mapBookToEntry).
                collect(Collectors.toList());
        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }

    @RequestMapping(value = "/sequence/{id}", produces = "application/atom+xml")
    public ModelAndView getBookBySequence(@PathVariable(value = "id") long id) {

        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        Sequence sequence = sequenceService.getSequence(id);
        if (sequence == null) {
            return mav;
        }
        mav.addObject(OpdsView.TITLE, "Книги в серии " + sequence.getName());

        List<Entry> entries = sequence.getBookSequences().stream().
                sorted(Comparator.comparing(BookSequence::getSeqOrder)).map(BookSequence::getBook).
                map(OPDSController2::mapBookToEntry).
                collect(Collectors.toList());
        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }

    @RequestMapping(value = "/authorsequences/{id}", produces = "application/atom+xml")
    public ModelAndView getAuthorSequences(@PathVariable(value = "id") long id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        Author author = authorService.getAuthor(id);
        if (author == null) {
            return mav;
        }
        mav.addObject(OpdsView.TITLE, "Книжные сериии " + author.getName());
        List<Entry> entries = author.getSequences().map(OPDSController2::mapSequenceToEntry).
                collect(Collectors.toList());
        mav.addObject(OpdsView.ENTRIES, entries);
        return mav;
    }

    private static Entry mapSequenceToEntry(Sequence sequence) {
        Entry entry = new Entry();
        entry.setTitle(sequence.getName());
        Content content = new Content();
        content.setType("text/html");
        content.setValue("Количество книг в серии: " + sequence.getBookSequences().size());
        entry.setContents(Collections.singletonList(content));
        Link link = new Link();
        link.setHref("/opds/sequence/" + sequence.getId());
        link.setRel(null);
        link.setType("application/atom+xml");
        entry.setOtherLinks(Collections.singletonList(link));
        return entry;
    }


    private static Entry mapBookToEntry(Book book) {
        Entry entry = new Entry();
        entry.setId("book:" + book.getId());
        entry.setUpdated(Date.from(Instant.now()));
        entry.setTitle(book.getTitle());
        entry.setAuthors(book.getAuthors().stream().map(author -> {
            SyndPersonImpl person = new SyndPersonImpl();
            person.setName(author.getName());
            person.setUri("/opds/author/" + author.getId());
            return person;
        }).collect(Collectors.toList()));
        // TODO entry.setCategories();
        Content content = new Content();
        content.setType("text/html");
        content.setValue(book.getDescr());
        entry.setContents(Collections.singletonList(content));
        Link link = new Link();
        link.setHref("/book/loadFile/" + book.getId());
        link.setRel(null);
        link.setType("application/fb2+zip");
        entry.setOtherLinks(Collections.singletonList(link));
        return entry;
    }
}
