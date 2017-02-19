package com.patex.extlib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.service.BookService;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@SuppressWarnings("WeakerAccess")
public class ExtLib {

    static final String REQUEST_P_NAME = "uri";
    static final String FB2_TYPE = "application/fb2";

    private final ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService actionExecutor = Executors.newCachedThreadPool();

    private final Cache<String, Book> bookCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final ExtLibrary extLibrary;

    private static List<MapLink> mapLinks = new ArrayList<>();
    private static Logger log = LoggerFactory.getLogger(ExtLibrary.class);

    private final Pattern fileNamePattern = Pattern.compile("attachment; filename=\"([^\"]+)\"");


    static {
        mapLinks.add(new OpdsCatalogLink());
        mapLinks.add(new FB2Link());
    }


    private final BookService bookService;

    public ExtLib(ExtLibrary extLibrary, BookService bookService) {
        this.extLibrary = extLibrary;
        this.bookService = bookService;
    }


    public ExtLibFeed getData(Map<String, String> requestParams) throws LibException {
        return getData(requestParams.get(REQUEST_P_NAME));
    }

    private ExtLibFeed getData(String uri) throws LibException {
        SyndFeed feed = getFeed(uri);

        List<Entry> entries = feed.getEntries().stream().map(this::mapEntry).collect(Collectors.toList());
        if (entries.stream().
                anyMatch(entry ->
                        entry.getOtherLinks().stream().
                                anyMatch(link -> link.getType().contains(FB2_TYPE)))) {
            Entry downloadEntry = new Entry();
            downloadEntry.setId("download:" + uri);
            downloadEntry.setTitle("Download all " + feed.getTitle());
            Content content = new Content();
            content.setValue("Download all ");
            content.setType("html");
            downloadEntry.setContents(Collections.singletonList(content));
            Link link = new Link();
            link.setHref(mapToUri("/downloadAll?", uri));
            link.setType("application/atom+xml;profile=opds-catalog");
            downloadEntry.setOtherLinks(Collections.singletonList(link));
            entries.add(0, downloadEntry);
        }
        return new ExtLibFeed(feed.getTitle(), feed.getLink(), entries);
    }


    private SyndFeed getFeed(String uri) throws LibException {
        if (uri == null) {
            uri = extLibrary.getOpdsPath();
            if(!uri.startsWith("/")){
                uri="/"+uri;
            }
        }
        return getDataFromURL(uri, uc -> new SyndFeedInput().build(new XmlReader(uc)));
    }

    private <E> E getDataFromURL(String uri, ExtLibFunction<URLConnection, E> function) throws LibException {
        try {
            return connectionExecutor.submit(() -> {
                URL url = new URL(extLibrary.getUrl() + uri);
                URLConnection uc = url.openConnection();
                if (extLibrary.getLogin() != null) {
                    String userpass = extLibrary.getLogin() + ":" + extLibrary.getPassword();
                    String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
                    uc.setRequestProperty("Authorization", basicAuth);
                }
                return function.apply(uc);
            }).get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
            throw new LibException(e.getMessage(), e);
        }
    }

    private Entry mapEntry(SyndEntry entry) {
        Entry newEntry = new Entry();
        newEntry.setId(entry.getUri());
        newEntry.setTitleEx(mapContent(entry.getTitleEx()));
        List<Link> links = entry.getLinks().stream().
                map(this::mapLink).filter(Objects::nonNull).collect(Collectors.toList());
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

    private Link mapLink(SyndLink link) {
        for (MapLink mapLink : mapLinks) {
            if (mapLink.accept(link.getType())) {
                return mapLink.mapLink(link);
            }
        }
        return null;
    }

    public String action(String action, Map<String, String> params) throws LibException {
        if ("download".equals(action)) {
            Book book = downloadFromExtLib(params.get("type"), params.get(REQUEST_P_NAME));
            return "/book/loadFile/" + book.getId();
        } else if ("downloadAll".equals(action)) {
            String uri = params.get(REQUEST_P_NAME);
            actionExecutor.execute(() -> downloadAll(uri));
            return mapToUri("?", uri);
        }
        throw new LibException("Unknown action: " + action);
    }

    private void downloadAll(String uri) {
        try {
            ExtLibFeed data = getData(uri);
            data.getEntries().stream().flatMap(entry -> entry.getOtherLinks().stream()).
                    filter(link -> link.getType().contains(FB2_TYPE)).
                    forEach(link -> {
                        try {
                            Optional<NameValuePair> uriO = URLEncodedUtils.parse(link.getHref(), Charset.forName("UTF-8")).stream().
                                    filter(nvp -> nvp.getName().equals(REQUEST_P_NAME)).findFirst();
                            if(uriO.isPresent()) {
                                downloadFromExtLib(FB2_TYPE, uriO.get().getValue());
                            }
                        } catch (LibException e) {
                            log.error(e.getMessage(), e);
                        }
                    });
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Book downloadFromExtLib(String type, String uri) throws LibException {
        return getDataFromURL(uri, conn ->
            bookCache.get(uri, () -> {
                String contentDisposition = conn.getHeaderField("Content-Disposition");
                String fileName;
                if (contentDisposition != null) {
                    Matcher matcher = fileNamePattern.matcher(contentDisposition);
                    if (matcher.matches()) {
                        fileName = matcher.group(1);
                    } else {
                        fileName = UUID.randomUUID().toString() + "." + type;
                        log.warn("Unable to find fileName in Content-Disposition: {}", contentDisposition);
                    }
                } else {
                    fileName = UUID.randomUUID().toString() + "." + type;
                }
                return bookService.uploadBook(fileName, conn.getInputStream());
            })
        );
    }

    private static String mapToUri(String prefix, String href) {
        try {
            return prefix + REQUEST_P_NAME + "=" + URLEncoder.encode(href, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private interface MapLink {

        boolean accept(String type);

        Link mapLink(SyndLink link);

    }

    private static class OpdsCatalogLink implements MapLink {

        @Override
        public boolean accept(String type) {
            return type.contains("profile=opds-catalog");
        }

        @Override
        public Link mapLink(SyndLink link) {
            Link newLink = new Link();
            newLink.setHref(mapToUri("?", link.getHref()));
            newLink.setRel(link.getRel());
            newLink.setType(link.getType());
            return newLink;
        }

    }

    private static class FB2Link implements MapLink {
        @Override
        public boolean accept(String type) {
            return type.contains(FB2_TYPE);
        }

        @Override
        public Link mapLink(SyndLink link) {
            Link newLink = new Link();
            newLink.setHref(mapToUri("download?type=fb2&", link.getHref()));
            newLink.setRel(link.getRel());
            newLink.setType(link.getType());
            return newLink;
        }
    }

    @FunctionalInterface
    public interface ExtLibFunction<T, R> {

        R apply(T t) throws Exception;
    }
}
