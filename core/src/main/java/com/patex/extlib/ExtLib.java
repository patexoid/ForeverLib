package com.patex.extlib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.service.BookService;
import com.patex.service.ZUserService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@SuppressWarnings("WeakerAccess")
@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExtLib {

    static final String REQUEST_P_NAME = "uri";
    static final String FB2_TYPE = "application/fb2";
    static final String REL_NEXT = "next";
    static final String ACTION_DOWNLOAD = "download";
    static final String ACTION_DOWNLOAD_ALL = "downloadAll";
    private static List<MapLink> mapLinks = new ArrayList<>();

    static {
        mapLinks.add(new OpdsCatalogLink());
        mapLinks.add(new FB2Link());
    }

    private static Logger log = LoggerFactory.getLogger(ExtLibrary.class);

    public static final String PARAM_TYPE = "type";

    private final ExecutorService actionExecutor = Executors.newCachedThreadPool();

    private final Cache<String, Book> bookCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final ExtLibrary extLibrary;

    @Autowired
    private MessengerService messengerService;


    private final Pattern fileNamePattern = Pattern.compile("attachment; filename=\"([^\"]+)\"");

    @Autowired
    private BookService bookService;

    @Autowired
    private ExtLibConnectionService extLibConnectionService;

    @Autowired
    ZUserService userService;

    public ExtLib(ExtLibrary extLibrary) {
        this.extLibrary = extLibrary;
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
            content.setValue("Download all");
            content.setType("html");
            downloadEntry.setContents(Collections.singletonList(content));
            Link link = new Link();
            link.setHref(mapToUri("action/downloadAll?", uri));
            link.setType("application/atom+xml;profile=opds-catalog");
            downloadEntry.setOtherLinks(Collections.singletonList(link));
            entries.add(0, downloadEntry);
        }

        ArrayList<Link> links = new ArrayList<>();
        Optional<SyndLink> nextPage = feed.getLinks().stream().
                filter(syndLink -> REL_NEXT.equals(syndLink.getRel())).findFirst();
        nextPage.ifPresent(syndLink -> {
            Entry nextEntry = new Entry();
            nextEntry.setId("next:" + uri);
            nextEntry.setTitle("Next");
            Content content = new Content();
            content.setValue("Next Page");
            content.setType("html");
            nextEntry.setContents(Collections.singletonList(content));
            nextEntry.setOtherLinks(Collections.singletonList(mapLink(syndLink)));
            entries.add(nextEntry);
            links.add(mapLink(syndLink));
        });
        return new ExtLibFeed(feed.getTitle(), entries, links);
    }


    private SyndFeed getFeed(String uri) throws LibException {
        return getDataFromURL(uri, uc -> new SyndFeedInput().build(new XmlReader(uc)));
    }

    private <E> E getDataFromURL(String uri, ExtLibFunction<URLConnection, E> function) throws LibException {
        IOException ee = null;
        for (int i = 0; i < 3; i++) {
            try {
                return createConnection(uri).getData(function);
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IOException) {
                    ee = (IOException) e.getCause();
                    try {
                        Thread.sleep(10000L);
                    } catch (InterruptedException e1) {
                        //do nothing
                    }
                } else {
                    throw new LibException(e);
                }
                log.error(e.getMessage(), e);
            }
        }
        assert ee != null;
        throw new LibException(ee.getMessage(), ee);
    }

    private ExtLibConnectionService.ExtlibCon createConnection(String uri) throws LibException {
        ExtLibConnectionService.ExtlibCon connection =
                extLibConnectionService.openConnection(toUrl(uri));
        if (extLibrary.getProxyType() != null) {
            connection.setProxy(extLibrary.getProxyType(), extLibrary.getProxyHost(), extLibrary.getProxyPort());
        }
        if (extLibrary.getLogin() != null) {
            connection.setBasicAuthorization(extLibrary.getLogin(), extLibrary.getPassword());
        }
        return connection;
    }

    private String toUrl(String uri) {
        if (uri == null) {
            uri = extLibrary.getOpdsPath();
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }
        }
        return extLibrary.getUrl() + uri;
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
        if (content.getMode() != null) {
            newContent.setMode(content.getMode());
        }
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

    @Secured(ZUserService.USER)
    public String action(String action, Map<String, String> params) throws LibException {
        if (ACTION_DOWNLOAD.equals(action)) {
            Book book = downloadFromExtLib(params.get(PARAM_TYPE), params.get(REQUEST_P_NAME));
            return "/book/loadFile/" + book.getId();
        } else if (ACTION_DOWNLOAD_ALL.equals(action)) {
            String uri = params.get(REQUEST_P_NAME);
            ZUser user = userService.getCurrentUser();
            actionExecutor.execute(() -> downloadAll(user, uri, FB2_TYPE));
            return mapToUri("?", uri);
        }
        throw new LibException("Unknown action: " + action);
    }

    public void downloadAll(ZUser user, String uri, String type) {
        try {
            AtomicInteger downloaded = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            AtomicInteger emptyLinks = new AtomicInteger(0);
            while (uri != null) {
                String uri0 = uri;ExtLibFeed data = getData(uri0);
                data.getEntries().forEach(entry -> {
                    List<Link> downloadLinks =entry.getOtherLinks().stream().
                        filter(link -> link.getType().contains(type)).collect(Collectors.toList());
                        if (downloadLinks.isEmpty()) {
                        emptyLinks.incrementAndGet();
                    } else if (downloadLinks.size() > 1) {
                        //I'm to lazy to implement this
                        String warning = "Book vialink " + toUrl(uri0) + " have more that 1 download link for book type " + type +
                                "\nBook title:" + entry.getTitle();
                        log.warn(warning);
                        messengerService.toRole(warning, ZUserService.ADMIN_AUTHORITY);
                    } else {
                            try {
                            Link link = downloadLinks.get(0);Optional<String> bookUri = extractExtUri(link);
                            if (bookUri.isPresent()) {
                                    downloadFromExtLib(link.getType(), bookUri.get());
                            downloaded.incrementAndGet();
                            } else {
                                emptyLinks.incrementAndGet();}
                            } catch (LibException e) {
                                log.error(e.getMessage(), e);failed.incrementAndGet();
                        }
                            }
                        });
                Optional<Link> nextLink = data.getLinks().stream().
                        filter(link -> REL_NEXT.equals(link.getRel())).findFirst();
                if (nextLink.isPresent()) {
                    Optional<String> nextExtUri = extractExtUri(nextLink.get());
                    uri = nextExtUri.orElse(null);
                } else {
                    uri = null;
                }
            }
            if (user != null) {
                messengerService.sendMessageToUser("" +
                                downloaded.get() + "books was downloaded\n" +
                                emptyLinks.get() + "wasn't found\n" +
                                failed.get() + "failed"
                        , user);
            }
        } catch (
                LibException e)

        {
            log.error(e.getMessage(), e);
        }
    }

    private Optional<String> extractExtUri(Link link) {
        String href = link.getHref();
        if (href.startsWith("?")) {
            href = href.substring(1);
        }
        Optional<NameValuePair> uriO =
                URLEncodedUtils.parse(href, Charset.forName("UTF-8")).stream().
                        filter(nvp -> nvp.getName().equals(REQUEST_P_NAME)).findFirst();
        return uriO.map(NameValuePair::getValue);
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

    static String mapToUri(String prefix, String href) {
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
            newLink.setHref(mapToUri("action/download?type=fb2&", link.getHref()));
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
