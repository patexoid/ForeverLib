package com.patex.forever.opds.model;

import com.patex.forever.model.Res;
import com.patex.forever.model.User;
import com.patex.forever.model.UserConfig;
import com.patex.forever.opds.model.converter.OPDSAuthor;
import com.patex.forever.service.Resources;
import com.patex.forever.service.UserService;
import com.rometools.rome.feed.atom.*;
import com.rometools.rome.feed.synd.SyndPerson;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Service(OpdsView.OPDS_VIEW)
public class OpdsView extends AbstractAtomFeedView {

    public static final String OPDS_VIEW = "opdsView";
    public static final String ENTRIES = "Entries";
    public static final String OPDS_METADATA = "opdsMetaData";

    private final UserService userService;

    private final Resources res;

    public OpdsView(UserService userService, Resources res) {
        setFeedType("opds.atom_1.0");
        this.userService = userService;
        this.res = res;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
                                           HttpServletRequest request, HttpServletResponse response) {
        List<OPDSEntry> entries = (List<OPDSEntry>) model.get(ENTRIES);
        User user = userService.getCurrentUser();
        UserConfig userConfig = user.getUserConfig();
        Locale locale;
        if (userConfig != null) {
            locale = userConfig.getLocale();
        } else {
            locale = Locale.getDefault();
        }
        return entries.stream().map(entry -> toEntry(entry, locale)).collect(Collectors.toList());
    }

    private Entry toEntry(OPDSEntry opdsEntryI, Locale locale) {

        Entry entry = new Entry();
        entry.setId(String.valueOf(opdsEntryI.getId()));
        if (opdsEntryI.getUpdated() != null) {
            entry.setUpdated(Date.from(opdsEntryI.getUpdated()));
        }
        Res title = opdsEntryI.getTitle();

        entry.setTitle(title.getMessage(res, locale));
        List<Content> content = toRomeContent(locale, opdsEntryI.getContent());
        entry.setContents(content);
        entry.setOtherLinks(opdsEntryI.getLinks().stream().map(this::toLink).collect(Collectors.toList()));
        entry.setAuthors(opdsEntryI.getAuthors().stream().map(this::toPerson).collect(Collectors.toList()));
        return entry;
    }

    private List<Content> toRomeContent(Locale locale, List<OPDSContent> contents) {
        return contents.stream().map(s -> {
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
        User user = userService.getCurrentUser();
        UserConfig userConfig = user.getUserConfig();
        Locale locale;
        if (userConfig != null) {
            locale = userConfig.getLocale();
        } else {
            locale = Locale.getDefault();
        }
        super.buildFeedMetadata(model, feed, request);
        OPDSMetadata metadata = (OPDSMetadata) model.get(OPDS_METADATA);
        Res title = metadata.getTitle();
        feed.setTitle(title.getMessage(res, locale));
        feed.setId(metadata.getId());
        feed.setUpdated(Date.from(metadata.getUpdated()));
        feed.setIcon("favicon.ico");
        feed.setEncoding("utf-8");
        //noinspection unchecked
        feed.setOtherLinks(metadata.getOtherLinks());
    }
}
