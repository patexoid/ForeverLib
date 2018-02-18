package com.patex.opds;

import com.patex.entities.ZUser;
import com.patex.entities.ZUserConfig;
import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSEntryI;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.Resources;
import com.patex.service.ZUserService;
import com.patex.utils.Res;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service(OpdsView.OPDS_VIEW)
public class OpdsView extends AbstractAtomFeedView {

    public static final String OPDS_VIEW = "opdsView";
    public static final String ENTRIES = "Entries";
    public static final String OPDS_METADATA = "opdsMetaData";

    @Autowired
    private ZUserService userService;

    @Autowired
    private Resources res;

    public OpdsView() {
        setFeedType("opds.atom_1.0");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<OPDSEntryI> entries = (List<OPDSEntryI>) model.get(ENTRIES);
        ZUser user = userService.getCurrentUser();
        ZUserConfig userConfig = user.getUserConfig();
        Locale locale;
        if (userConfig != null) {
            locale = userConfig.getLocale();
        } else {
            locale = Locale.getDefault();
        }
        return entries.stream().map(entry -> toEntry(entry, locale)).collect(Collectors.toList());
    }

    private Entry toEntry(OPDSEntryI opdsEntryI, Locale locale) {

        Entry entry = new Entry();
        entry.setId(String.valueOf(opdsEntryI.getId()));
        if (opdsEntryI.getUpdated() != null) {
            entry.setUpdated(opdsEntryI.getUpdated());
        }
        Res title = opdsEntryI.getTitle();

        entry.setTitle(res.get(locale, title.getKey(), title.getObjs()));
        Optional<List<Content>> content = opdsEntryI.getContent().map(contents -> toRomeContent(locale, contents));
        content.ifPresent(entry::setContents);

        entry.setOtherLinks(opdsEntryI.getLinks().stream().map(this::toLink).collect(Collectors.toList()));
        opdsEntryI.getAuthors().ifPresent(opdsAuthors ->
                entry.setAuthors(opdsAuthors.stream().map(this::toPerson).collect(Collectors.toList())));
        return entry;
    }

    private List<Content> toRomeContent(Locale locale, List<OPDSContent> contents) {
        return contents.stream().map(s ->{
            Content content = new Content();
            content.setValue(s.getValue(res, locale));
            content.setType(s.getType());
            content.setSrc(s.getSrc());
            return content;
        }).reduce(this::reduceContent).map(Collections::singletonList).orElseGet(ArrayList::new);
    }

    private Content reduceContent(Content first, Content second) {
        Content newContent = new Content();
        String type = first.getType();
        String newValue;
        if (type.toLowerCase().contains("html")) {
            newValue = first.getValue() + "<br/>" + second.getValue();
        } else {
            newValue = first.getValue() + "\n " + second.getValue();

        }
        newContent.setValue(newValue);
        newContent.setType(type);
        newContent.setSrc(first.getSrc());
        return newContent;
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
        ZUser user = userService.getCurrentUser();
        ZUserConfig userConfig = user.getUserConfig();
        Locale locale;
        if (userConfig != null) {
            locale = userConfig.getLocale();
        } else {
            locale = Locale.getDefault();
        }
        super.buildFeedMetadata(model, feed, request);
        OPDSMetadata metadata = (OPDSMetadata) model.get(OPDS_METADATA);
        Res title = metadata.getTitle();
        feed.setTitle(res.get(locale, title.getKey(), title.getObjs()));
        feed.setId(metadata.getId());
        feed.setUpdated(metadata.getUpdated());
        feed.setIcon("favicon.ico");
        feed.setEncoding("utf-8");
        //noinspection unchecked
        feed.setOtherLinks(metadata.getOtherLinks());
    }
}
