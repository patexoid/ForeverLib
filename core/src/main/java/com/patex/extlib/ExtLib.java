package com.patex.extlib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.opds.OPDSEntryI;
import com.patex.opds.OPDSEntryImpl;
import com.patex.opds.OPDSLink;
import com.patex.service.BookService;
import com.patex.service.ZUserService;
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
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import static com.patex.opds.OPDSLink.FB2;
import static com.patex.opds.OPDSLink.OPDS_CATALOG;

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

    public ExtLibFeed getExtLibFeed(Map<String, String> requestParams) throws LibException {
        return getExtLibFeed(requestParams.get(REQUEST_P_NAME));
    }

    private ExtLibFeed getExtLibFeed(String uri) throws LibException {
        SyndFeed feed = getFeed(uri);
        List<OPDSEntryI> entries = feed.getEntries().stream().map(ExtLibOPDSEntry::new).
                collect(Collectors.toList());
        if (entries.stream().
                anyMatch(entry ->
                        entry.getLinks().stream().
                                anyMatch(link -> link.getType().contains(FB2)))) {
            OPDSEntryI downloadEntry =
                    new OPDSEntryImpl("download:" + uri, new Date(), "Download all " + feed.getTitle(), "Download all",
                            new OPDSLink(ExtLibOPDSEntry.mapToUri("action/downloadAll?", uri), OPDS_CATALOG)
                    );
            entries.add(0, downloadEntry);
        }

        ArrayList<OPDSLink> links = new ArrayList<>();
        Optional<SyndLink> nextPage = feed.getLinks().stream().
                filter(syndLink -> REL_NEXT.equals(syndLink.getRel())).findFirst();
        nextPage.ifPresent(syndLink -> {
            OPDSEntryI nextEntry = new OPDSEntryImpl("next:" + uri, new Date(), "Next", "Next Page",
                    ExtLibOPDSEntry.mapLink(syndLink));

            entries.add(nextEntry);
            links.add(ExtLibOPDSEntry.mapLink(syndLink));
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

    @Secured(ZUserService.USER)
    public String action(String action, Map<String, String> params) throws LibException {
        if (ACTION_DOWNLOAD.equals(action)) {
            Book book = downloadFromExtLib(params.get(PARAM_TYPE), params.get(REQUEST_P_NAME));
            return "/book/loadFile/" + book.getId();
        } else if (ACTION_DOWNLOAD_ALL.equals(action)) {
            String uri = params.get(REQUEST_P_NAME);
            ZUser user = userService.getCurrentUser();
            actionExecutor.execute(() -> downloadAll(user, uri, FB2_TYPE));
            return ExtLibOPDSEntry.mapToUri("?", uri);
        }
        throw new LibException("Unknown action: " + action);
    }

    public void downloadAll(ZUser user, String uri, String type) {
        try {
            AtomicInteger downloaded = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);
            AtomicInteger emptyLinks = new AtomicInteger(0);
            while (uri != null) {
                String uri0 = uri;
                ExtLibFeed data = getExtLibFeed(uri0);
                data.getEntries().forEach(entry -> {
                    List<OPDSLink> downloadLinks = entry.getLinks().stream().
                            filter(link -> link.getType().contains(type)).collect(Collectors.toList());
                    if (downloadLinks.isEmpty()) {
                        emptyLinks.incrementAndGet();
                    } else if (downloadLinks.size() > 1) {
                        //I'm to lazy to implement this
                        String warning = "Book via link " + toUrl(uri0) + " have more that 1 download link for book type " + type +
                                "\nBook title:" + entry.getTitle();
                        log.warn(warning);
                        messengerService.toRole(warning, ZUserService.ADMIN_AUTHORITY);
                    } else {
                        try {
                            OPDSLink link = downloadLinks.get(0);
                            Optional<String> bookUri = extractExtUri(link);
                            if (bookUri.isPresent()) {
                                downloadFromExtLib(link.getType(), bookUri.get());
                                downloaded.incrementAndGet();
                            } else {
                                emptyLinks.incrementAndGet();
                            }
                        } catch (LibException e) {
                            log.error(e.getMessage(), e);
                            failed.incrementAndGet();
                        }
                    }
                });
                Optional<String> nextLink = data.getLinks().stream().
                        filter(link -> REL_NEXT.equals(link.getRel())).
                        findFirst().map(link -> extractExtUri(link).orElse(null));
                uri = nextLink.orElse(null);
            }
            if (user != null) {
                messengerService.sendMessageToUser("" +
                                downloaded.get() + " books was downloaded\n" +
                                emptyLinks.get() + " wasn't found\n" +
                                failed.get() + " failed"
                        , user);
            }
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static Optional<String> extractExtUri(OPDSLink link) {
        String href = link.getHref();
        if (href.startsWith("?")) {
            href = href.substring(1);
        }
        Optional<NameValuePair> uriO =
                URLEncodedUtils.parse(href, Charset.forName("UTF-8")).stream().
                        filter(nvp -> nvp.getName().equals(REQUEST_P_NAME)).findFirst();
        return uriO.map(NameValuePair::getValue);
    }

    private Book downloadFromExtLib(String type, String uri) throws LibException {
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

    @FunctionalInterface
    public interface ExtLibFunction<T, R> {

        R apply(T t) throws Exception;
    }
}
