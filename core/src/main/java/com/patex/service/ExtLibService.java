package com.patex.service;

import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ExtLibraryRepository;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 *
 *
 */


@Service
public class ExtLibService {

    public static final String EXT_LIB = "extLib";
    public static final String REQUEST_P_NAME = "uri";

    @Autowired
    private ExtLibraryRepository extLibRepo;

    @Autowired
    private BookService bookService;

    private List<MapLink> mapLinks =new ArrayList<>();
    private final Pattern fileNamePattern=Pattern.compile("attachment; filename=\"([^\"]+)\"");
    private static Logger log = LoggerFactory.getLogger(ExtLibService.class);

    @PostConstruct
    public void setUp(){
        mapLinks.add(new OpdsCatalogLink());
        mapLinks.add(new FB2Link());
    }


    public List<Entry> getDataForLibrary(Long libId, String uri, String urlPrefix) throws LibException{
        try {
            SyndFeed feed = getFeed(libId, uri);
            return feed.getEntries().stream().
                    map(entry -> mapEntry(urlPrefix, entry)).collect(Collectors.toList());
        } catch (IOException | FeedException e) {
            throw new LibException(e);
        }
    }

    private Entry mapEntry(String urlPrefix, SyndEntry entry) {
        Entry newEntry = new Entry();
        newEntry.setId(entry.getUri());
        newEntry.setTitleEx(mapContent(entry.getTitleEx()));
        List<Link> links = entry.getLinks().stream().
                map(link -> mapLink(urlPrefix, link)).filter(Objects::nonNull).collect(Collectors.toList());
        newEntry.setOtherLinks(links);

        List<Content> contents = entry.getContents().stream().
                map(this::mapContent).collect(Collectors.toList());
        newEntry.setContents(contents);
        return newEntry;
    }

    private Content mapContent(SyndContent content) {
        Content newContent = new Content();
        newContent.setType(content.getType());
        newContent.setValue(content.getValue());
        content.setMode(content.getMode());
        return newContent;
    }

    private Link mapLink(String urlPrefix, SyndLink link) {
        for (MapLink mapLink : mapLinks) {
            if(mapLink.accept(link.getType())){
                return mapLink.mapLink(link, urlPrefix);
            }
        }
        return null;
    }

    private SyndFeed getFeed(Long libId, String uri) throws IOException, FeedException {
        ExtLibrary library = extLibRepo.findOne(libId);
        if (uri == null) {
            uri = library.getOpdsPath();
        }

        URL url = new URL(library.getUrl() + "/" + uri);
        URLConnection uc = url.openConnection();
        if (library.getLogin() != null) {
            String userpass = library.getLogin() + ":" + library.getPassword();
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            uc.setRequestProperty("Authorization", basicAuth);
        }

        SyndFeedInput input = new SyndFeedInput();
        return input.build(new XmlReader(uc));
    }

    public Iterable<ExtLibrary> findAll() {
        return extLibRepo.findAll();
    }

    public Book downloadFromExtLib(long id, String type, String uri) throws IOException, LibException {
        ExtLibrary library = extLibRepo.findOne(id);
        URLConnection conn=new URL(library.getUrl()+"/"+uri).openConnection();
        String contentDisposition = conn.getHeaderField("Content-Disposition");
        String fileName;
        if(contentDisposition!=null){
            Matcher matcher = fileNamePattern.matcher(contentDisposition);
            if(matcher.matches()){
                fileName=matcher.group(1);
            } else {
                fileName = UUID.randomUUID().toString() + "." + type;
                log.warn("Unable to find fileName in Content-Disposition: {}", contentDisposition);
            }
        } else {
            fileName= UUID.randomUUID().toString()+ "." + type;
        }
        return bookService.uploadBook(fileName, conn.getInputStream());
    }


    private interface MapLink {

        boolean accept(String type);
        Link mapLink(SyndLink link, String urlPrefix);
    }

    private class OpdsCatalogLink implements MapLink {
        @Override
        public boolean accept(String type) {
            return type.contains("profile=opds-catalog");
        }

        @Override
        public Link mapLink(SyndLink link, String urlPrefix) {
            Link newLink = new Link();
            try {
                newLink.setHref(urlPrefix + "?" + REQUEST_P_NAME + "=" + URLEncoder.encode(link.getHref(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(),e);
            }
            newLink.setRel(link.getRel());
            newLink.setType(link.getType());
            return newLink;
        }
    }

    private class FB2Link implements MapLink {
        @Override
        public boolean accept(String type) {
            return type.contains("application/fb2");
        }

        @Override
        public Link mapLink(SyndLink link, String urlPrefix) {
            Link newLink = new Link();
            try {
                newLink.setHref(urlPrefix + "/fb2?" + REQUEST_P_NAME + "=" + URLEncoder.encode(link.getHref(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(),e);
            }
            newLink.setRel(link.getRel());
            newLink.setType(link.getType());
            return newLink;
        }
    }

}
