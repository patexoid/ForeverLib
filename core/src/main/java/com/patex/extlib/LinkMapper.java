package com.patex.extlib;

import com.patex.opds.OPDSLink;
import com.rometools.rome.feed.synd.SyndLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.patex.opds.OPDSLink.FB2;

public class LinkMapper {

    private static final Logger log = LoggerFactory.getLogger(LinkMapper.class);
    private static final List<MapLink> mapLinks = new ArrayList<>(2);

    static {
        LinkMapper.mapLinks.add(new OpdsCatalogLink());
        LinkMapper.mapLinks.add(new FB2Link());
    }

    public static String mapToUri(String prefix, String href) {
        try {
            return prefix + ExtLibService.REQUEST_P_NAME + "=" + URLEncoder.encode(href, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
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

    static class OpdsCatalogLink implements MapLink {

        @Override
        public boolean accept(String type) {
            return type.contains("profile=opds-catalog");
        }

        @Override
        public OPDSLink mapLink(SyndLink link) {
            return new OPDSLink(mapToUri("?", link.getHref()), link.getRel(), link.getType());
        }
    }

    static class FB2Link implements MapLink {
        @Override
        public boolean accept(String type) {
            return type.contains(FB2);
        }

        @Override
        public OPDSLink mapLink(SyndLink link) {
            return new OPDSLink(mapToUri("download?type=fb2&", link.getHref()), link.getRel(), link.getType());
        }
    }
}
