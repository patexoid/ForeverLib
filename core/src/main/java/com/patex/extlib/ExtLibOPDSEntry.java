package com.patex.extlib;

import com.patex.opds.OPDSEntryI;
import com.patex.opds.OPDSLink;
import com.patex.utils.LinkUtils;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.patex.opds.OPDSLink.FB2;

/**
 * Created by Alexey on 07.06.2017.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ExtLibOPDSEntry implements OPDSEntryI {
    private static Logger log = LoggerFactory.getLogger(ExtLibOPDSEntry.class);

    private static List<MapLink> mapLinks = new ArrayList<>(2);

    static {
        mapLinks.add(new OpdsCatalogLink());
        mapLinks.add(new FB2Link());
    }


    private final String id;
    private final String title;
    private final List<OPDSLink> links;
    private final Optional<Date> updated;
    private final Optional<List<String>> content;


    public ExtLibOPDSEntry(SyndEntry syndEntry) {
        id = syndEntry.getUri();
        title = syndEntry.getTitle();
        links = syndEntry.getLinks().stream().
                map(ExtLibOPDSEntry::mapLink).filter(Objects::nonNull).collect(Collectors.toList());
        updated = Optional.ofNullable(syndEntry.getUpdatedDate());
        content = Optional.of(syndEntry.getContents().stream().
                map(SyndContent::getValue).collect(Collectors.toList()));
    }

    ExtLibOPDSEntry(String id, String title, List<OPDSLink> links,
                    Optional<Date> updated, Optional<List<String>> collect) {
        this.id = id;
        this.title = title;
        this.links = links;
        this.updated = updated;
        content = collect;
    }

    static String mapToUri(String prefix, String href) {
        try {
            return prefix + ExtLib.REQUEST_P_NAME + "=" + URLEncoder.encode(href, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Optional<Date> getUpdated() {
        return updated;
    }

    @Override
    public Optional<List<String>> getContent() {
        return content;
    }


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

    public ExtLibOPDSEntry updateLinksPrefix(String prefix) {
        List<OPDSLink> links = this.links.stream().
                map(link -> new OPDSLink(LinkUtils.makeURL(prefix, link.getHref()), link.getRel(), link.getType())).
                collect(Collectors.toList());
        return new ExtLibOPDSEntry(id, title, links, updated, content);
    }


    static OPDSLink mapLink(SyndLink link) {
        for (MapLink mapLink : mapLinks) {
            if (mapLink.accept(link.getType())) {
                return mapLink.mapLink(link);
            }
        }
        return null;
    }

    private interface MapLink {

        boolean accept(String type);

        OPDSLink mapLink(SyndLink link);

    }

    private static class OpdsCatalogLink implements MapLink {

        @Override
        public boolean accept(String type) {
            return type.contains("profile=opds-catalog");
        }

        @Override
        public OPDSLink mapLink(SyndLink link) {
            return new OPDSLink(mapToUri("?", link.getHref()),
                    link.getRel(), link.getType());
        }

    }

    private static class FB2Link implements MapLink {
        @Override
        public boolean accept(String type) {
            return type.contains(FB2);
        }

        @Override
        public OPDSLink mapLink(SyndLink link) {
            return new OPDSLink(mapToUri("action/download?type=fb2&", link.getHref()),
                    link.getRel(), link.getType());
        }
    }
}
