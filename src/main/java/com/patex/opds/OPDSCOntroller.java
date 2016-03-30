package com.patex.opds;

import com.patex.entities.AggrResult;
import com.patex.service.AuthorService;
import com.patex.service.BookService;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("opds")
public class OPDSController {

    public static final int EXPAND_FOR_AUTHORS_COUNT = 3;

    @Autowired
    AuthorService authorService;

    @Autowired
    BookService book;

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

        List<Entry> authors = authorsCount.parallelStream().
                filter(aggrResult -> aggrResult.getResult() > EXPAND_FOR_AUTHORS_COUNT || authorsCount.size() == 1).
                flatMap(aggrResult -> authorService.findByName(aggrResult.getId()).stream()).
                map(author ->
                        createEntry("" + author.getId(), author.getName(),
                                "/opds/author/" + author.getId(),
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
}
