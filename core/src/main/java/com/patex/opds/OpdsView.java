package com.patex.opds;

import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSEntryI;
import com.patex.opds.converters.OPDSLink;
import com.rometools.rome.feed.atom.Content;
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
    public static final String ENTRIES = "Entries";
    public static final String OPDS_METADATA="opdsMetaData";


    public OpdsView() {
        setFeedType("opds.atom_1.0");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<OPDSEntryI> entries = (List<OPDSEntryI>) model.get(ENTRIES);
        return entries.stream().map(this::toEntry).collect(Collectors.toList());
    }

    private Entry toEntry(OPDSEntryI opdsEntryI) {
        Entry entry = new Entry();
        entry.setId(String.valueOf(opdsEntryI.getId()));
        if(opdsEntryI.getUpdated()!=null) {
            entry.setUpdated(opdsEntryI.getUpdated());
        }
        entry.setTitle(opdsEntryI.getTitle());
        opdsEntryI.getContent().ifPresent(contents -> {
            entry.setContents(contents.stream().map(s -> {
                Content content = new Content();
                content.setValue(s.getValue());
                content.setType(s.getType());
                content.setSrc(s.getSrc());
                return content;
            }).collect(Collectors.toList()));
        });
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
        OPDSMetadata  metadata = (OPDSMetadata) model.get(OPDS_METADATA);
        feed.setTitle(metadata.getTitle());
        feed.setId(metadata.getId());
        feed.setUpdated(metadata.getUpdated());
        feed.setIcon("favicon.ico");
        //noinspection unchecked
        feed.setOtherLinks(metadata.getOtherLinks());
    }
}
