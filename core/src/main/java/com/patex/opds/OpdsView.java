package com.patex.opds;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service(OpdsView.OPDS_VIEW)
public class OpdsView extends AbstractAtomFeedView {

    public static final String OPDS_VIEW = "opdsView";
    public static final String TITLE = "Title";
    public static final String ENTRIES = "Entries";
    public static final String LINKS = "LINKS";

    @SuppressWarnings("unchecked")
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<OPDSEntryI> entries = (List<OPDSEntryI>) model.get(ENTRIES);
        return entries.stream().map(this::toEntry).collect(Collectors.toList());
    }

    private Entry toEntry(OPDSEntryI opdsEntryI) {
        Entry entry = new Entry();
        entry.setId(String.valueOf(opdsEntryI.getId()));
        opdsEntryI.getUpdated().ifPresent(entry::setUpdated);
        entry.setTitle(opdsEntryI.getTitle());
        entry.setOtherLinks(opdsEntryI.getLinks().stream().map(this::toLink).collect(Collectors.toList()));
        opdsEntryI.getAuthors().ifPresent(opdsAuthors ->
                entry.setAuthors(opdsAuthors.stream().map(this::toPerson).collect(Collectors.toList())));
        return entry;
    }

    private Link toLink(OPDSLink opdsLinkI) {
        Link link = new Link();
        link.setHref(opdsLinkI.getHref());
        link.setRel(opdsLinkI.getRel());
        link.setType(opdsLinkI.getType());
        return link;
    }

    private SyndPerson toPerson(OPDSAuthor opdsAuthorI) {
        Person person = new Person();
        person.setName(opdsAuthorI.getName());
        person.setUri(opdsAuthorI.getUri());
        return person;
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
        super.buildFeedMetadata(model, feed, request);
        String title = (String) model.get(TITLE);
        feed.setTitle(title);
        feed.setId(title);
//        feed.setUpdated(Date.from(Instant.now()));
        feed.setIcon("favicon.ico");
        //noinspection unchecked
        feed.setOtherLinks((List<Link>) model.get(LINKS));
    }
}
