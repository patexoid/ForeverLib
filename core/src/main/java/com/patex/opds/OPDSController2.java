package com.patex.opds;

import com.patex.entities.*;
import com.patex.service.AuthorService;
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

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Controller
@RequestMapping(OPDSController2.PREFIX)
public class OPDSController2 {

    public static final String PREFIX = "opds";
    private static final String AUTHORSINDEX = "authorsindex";
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";
    private static Logger log = LoggerFactory.getLogger(OPDSController2.class);

    private static final int EXPAND_FOR_AUTHORS_COUNT = 3;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private SequenceService sequenceService;
    private final List<Entry> rootEntries = new ArrayList<>();

    @PostConstruct
    public void setUp(){
        rootEntries.add(createEntry("root:authors", "По Авторам", makeURL(PREFIX, AUTHORSINDEX)));
    }


    public void addRoot( Entry entry){
        rootEntries.add(entry);

    }

    @RequestMapping(produces = "application/atom+xml")
    public ModelAndView getMain() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        mav.addObject(OpdsView.TITLE, "Zombie Catalog");
        return createMav("Zombie Catalog", rootEntries);
    }

    @RequestMapping(value = AUTHORSINDEX, produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex() {
        return getAuthorsIndex("");
    }

    @RequestMapping(value = AUTHORSINDEX + "/{start}", produces = "application/atom+xml")
    public ModelAndView getAuthorsIndex(@PathVariable(value = "start") String start) {
        return createMav("", authorService.getAuthorsCount(start), aggrResults -> {
            List<Entry> entries = new ArrayList<>();
            List<Entry> authors = aggrResults.stream().
                    filter(aggrResult -> aggrResult.getResult() < EXPAND_FOR_AUTHORS_COUNT).
                    flatMap(aggrResult -> authorService.findByName(aggrResult.getId()).stream()).
                    map(author ->
                            createEntry("" + author.getId(), author.getName(),
                                    makeURL("opds","author",author.getId()),
                                    makeURL("opds","authorsequences",author.getId()),
                                    makeURL("opds","authorsequenceless",author.getId()))).
                    sorted(Comparator.comparing(Entry::getTitle)).
                    collect(Collectors.toList());
            entries.addAll(authors);

            List<Entry> serachEntries = aggrResults.stream().
                    filter(aggrResult -> aggrResult.getResult() > EXPAND_FOR_AUTHORS_COUNT && aggrResults.size() != 1).
                    map(aggr -> createEntry(aggr.getId(), aggr.getId(), makeURL("opds",AUTHORSINDEX,aggr.getId()))).
                    collect(Collectors.toList());
            entries.addAll(serachEntries);
            return entries;
        });
    }

    @RequestMapping(value = "author/{id}", produces = "application/atom+xml")
    public ModelAndView getAuthor(@PathVariable(value = "id") long id) {
        return createMav("", authorService.getAuthors(id), author -> {
            List<Entry> entries = new ArrayList<>();
            Entry entry = new Entry();
            entry.setTitle("Книги автора " + author.getName());
            Content content = new Content();
            content.setType("text/html");
            content.setValue(author.getDescr());
            entry.setContents(Collections.singletonList(content));
            entries.add(entry);
            entries.add(createEntry("" + author.getId(), author.getName() + "Книги по алфавиту",
                    makeURL("opds","author",author.getId()),"alphabet"));
            entries.add(createEntry("" + author.getId(), author.getName() + "Книги по сериям",
                    makeURL("opds","authorsequences",author.getId())));
            return entries;
        });
    }

    @RequestMapping(value = "author/{id}/alphabet", produces = "application/atom+xml")
    public ModelAndView getAuthorBookAlphabet(@PathVariable(value = "id") long id) {
        Author bookAuthor = authorService.getAuthors(id);
        return createMav("Книги по алфавиту " + bookAuthor.getName(), bookAuthor, author ->
                author.getBooks().stream().
                        map(AuthorBook::getBook).
                        map(OPDSController2::mapBookToEntry).
                        collect(Collectors.toList()));
    }

    @RequestMapping(value = "sequence/{id}", produces = "application/atom+xml")
    public ModelAndView getBookBySequence(@PathVariable(value = "id") long id) {

        Sequence sequence = sequenceService.getSequence(id);
        return createMav("Книги в серии " + sequence.getName(), sequence, seq ->
                seq.getBookSequences().stream().
                        sorted(Comparator.comparing(BookSequence::getSeqOrder)).map(BookSequence::getBook).
                        map(OPDSController2::mapBookToEntry).
                        collect(Collectors.toList())
        );
    }

    @RequestMapping(value = "authorsequences/{id}", produces = APPLICATION_ATOM_XML)
    public ModelAndView getAuthorSequences(@PathVariable(value = "id") long id) {
        Author author = authorService.getAuthors(id);
        return createMav("Книжные сериии " + author.getName(), author,
                a -> a.getSequencesStream().map(OPDSController2::mapSequenceToEntry).
                        collect(Collectors.toList()));
    }



    private static Entry mapSequenceToEntry(Sequence sequence) {
        Entry entry = new Entry();
        entry.setId("sequence:" + sequence.getId());
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
        entry.setAuthors(book.getAuthorBooks().stream().map(AuthorBook::getAuthor).map(author -> {
            SyndPersonImpl person = new SyndPersonImpl();
            person.setName(author.getName());
            person.setUri("/opds/author/" + author.getId());
            return person;
        }).collect(Collectors.toList()));
        // TODO entry.setCategories();
        Content content = new Content();
        content.setType("text/html");
        String descr=book.getSequences().stream().
                map(bs -> "Серия:" + bs.getSequence().getName() +" #" + bs.getSeqOrder()+"&lt;br/&gt;").
                reduce(book.getDescr(),String::concat);
        content.setValue(descr.replaceAll("\n","&lt;br/&gt;"));
        entry.setContents(Collections.singletonList(content));
        Link link = new Link();
        link.setHref("/book/loadFile/" + book.getId());
        link.setRel(null);
        link.setType("application/fb2+zip");
        entry.setOtherLinks(Collections.singletonList(link));
        return entry;
    }

    public static Entry createEntry(String id, String title, String... hrefs) {
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

    public static String makeURL(Object... parts) {
        return Arrays.stream(parts).map(String::valueOf).reduce("", (s, s2) -> s + "/" + s2);
    }

    public static <E> ModelAndView createMav(String title, E e, Function<E, List<Entry>> func) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName(OpdsView.OPDS_VIEW);
        if (e != null) {
            List<Entry> entries = func.apply(e);
            mav.addObject(OpdsView.ENTRIES, entries);
        } else {
            log.warn("empty obj:" +title);
        }
        mav.addObject(OpdsView.TITLE, title);
        return mav;
    }

    public static ModelAndView createMav(String title, List<Entry> entries) {
        return createMav(title, entries, e -> e);
    }
}
