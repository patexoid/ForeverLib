package com.patex.extlib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.SavedBook;
import com.patex.entities.Subscription;
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

import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    static final String ACTION_SUBSCRIBE = "subscribe";

    private static Logger log = LoggerFactory.getLogger(ExtLibrary.class);

    public static final String PARAM_TYPE = "type";

    private final ExecutorService actionExecutor = Executors.newCachedThreadPool();
    private final ExecutorService subscriptionExecutor = Executors.newSingleThreadExecutor();

    private final Cache<String, Book> bookCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

    private final ExtLibrary extLibrary;

    @Autowired
    private MessengerService messengerService;

    @Autowired
    private ExtLibService extLibService;

    private final Pattern fileNamePattern = Pattern.compile("attachment; filename=\"([^\"]+)\"");

    @Autowired
    private BookService bookService;

    @Autowired
    private ExtLibConnection extLibConnectionService;

    @Autowired
    ZUserService userService;

    public ExtLib(ExtLibrary extLibrary) {
        this.extLibrary = extLibrary;
    }

    public ExtLibFeed getExtLibFeed(Map<String, String> requestParams) throws LibException {
        String uri = requestParams.get(REQUEST_P_NAME);
        ExtLibFeed feed = getExtLibFeed(uri);
        List<OPDSEntryI> entries = feed.getEntries();
        if (entries.stream().flatMap(entry -> entry.getLinks().stream()).
                anyMatch(link -> link.getType().contains(FB2))) {
            if (extLibrary.getSubscriptions().stream().
                    noneMatch(subscription -> uri.equals(subscription.getLink()))) {
                entries.add(0, new OPDSEntryImpl("subscribe:" + uri, new Date(),
                        "Subscribe to " + feed.getTitle(), "Subscribe",
                        new OPDSLink(ExtLibOPDSEntry.mapToUri("action/subscribe?", uri), OPDS_CATALOG)));
            }

            entries.add(0, new OPDSEntryImpl("download:" + uri, new Date(), "Download all " + feed.getTitle(),
                    "Download all",
                    new OPDSLink(ExtLibOPDSEntry.mapToUri("action/downloadAll?", uri), OPDS_CATALOG)
            ));
        }

        List<OPDSLink> links = feed.getLinks();
        feed.getLinks().stream().
                filter(link -> REL_NEXT.equals(link.getRel())).findFirst().
                ifPresent(nextLink -> {
                    OPDSEntryI nextEntry = new OPDSEntryImpl("next:" + uri, new Date(), "Next", "Next Page",
                            nextLink);
                    entries.add(nextEntry);
                });

        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    private ExtLibFeed getExtLibFeed(String uri) throws LibException {
        SyndFeed feed = getFeed(uri);
        List<OPDSEntryI> entries = feed.getEntries().stream().map(ExtLibOPDSEntry::new).
                collect(Collectors.toList());

        ArrayList<OPDSLink> links = new ArrayList<>();
        Optional<SyndLink> nextPage = feed.getLinks().stream().
                filter(syndLink -> REL_NEXT.equals(syndLink.getRel())).findFirst();
        nextPage.ifPresent(syndLink -> {
            links.add(ExtLibOPDSEntry.mapLink(syndLink));
        });
        return new ExtLibFeed(feed.getTitle(), entries, links);
    }


    private SyndFeed getFeed(String uri) throws LibException {
        return getDataFromURL(uri, uc -> new SyndFeedInput().build(new XmlReader(uc)));
    }

    private <E> E getDataFromURL(String uri, ExtLibFunction<URLConnection, E> function) throws LibException {
        ExtLibConnection.ExtlibCon connection = extLibConnectionService.openConnection(toUrl(uri));
        if (extLibrary.getProxyType() != null) {
            connection.setProxy(extLibrary.getProxyType(), extLibrary.getProxyHost(), extLibrary.getProxyPort());
        }
        if (extLibrary.getLogin() != null) {
            connection.setBasicAuthorization(extLibrary.getLogin(), extLibrary.getPassword());
        }
        return connection.getData(function);
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
            actionExecutor.execute(() -> downloadAll(user, uri));
            return ExtLibOPDSEntry.mapToUri("?", uri);
        } else if (ACTION_SUBSCRIBE.equals(action)) {
            return addSubscription(params.get(REQUEST_P_NAME));
        }
        throw new LibException("Unknown action: " + action);
    }

    private synchronized String addSubscription(String uri) {
        Predicate<Subscription> uriFilter = subscription -> uri.equals(subscription.getLink());
        if (extLibrary.getSubscriptions().stream().
                noneMatch(uriFilter)) {
            ZUser user = userService.getCurrentUser();
            extLibrary.addSubscription(uri, user);
            extLibService.save(extLibrary);
            actionExecutor.execute(() -> extLibrary.getSubscriptions().stream().
                    filter(uriFilter).findAny().
                    ifPresent(this::checkSubscription)
            );
        }
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }


    private Stream<OPDSEntryI> getAsEntryStream(String uri) throws LibException {
        Stream<OPDSEntryI> resultStream = Stream.empty();
        while (uri != null) {
            String uri0 = uri;
            ExtLibFeed data = getExtLibFeed(uri0);
            resultStream = Stream.concat(resultStream, data.getEntries().stream());
            Optional<String> nextLink = data.getLinks().stream().
                    filter(link -> REL_NEXT.equals(link.getRel())).
                    findFirst().map(link -> extractExtUri(link).orElse(null));
            uri = nextLink.orElse(null);
        }
        return resultStream;
    }


    public void downloadAll(ZUser user, String uri) {
        try {
            DownloadAllResult result = downloadAll(getAsEntryStream(uri), entry -> {
            });
            messengerService.sendMessageToUser("" +
                            result.getSuccess().size() + " books was downloaded\n" +
                            result.getEmpty() + " wasn't found\n" +
                            result.getFailed() + " failed"
                    , user);
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
    }

    private DownloadAllResult downloadAll(Stream<OPDSEntryI> entryStream, Consumer<String> postDownload) {
        AtomicInteger emptyCount = new AtomicInteger(0);
        List<Book> rawResult = entryStream.
                map(entry -> checkLinks(entry, emptyCount)).
                flatMap(entry -> entry.getLinks().
                        stream().filter(link -> link.getType().contains(FB2_TYPE))).
                map(ExtLib::extractExtUri).
                filter(Optional::isPresent).
                map(Optional::get).map(
                link -> {
                    try {
                        Book book = downloadFromExtLib("fb2", link);
                        postDownload.accept(link);
                        return book;
                    } catch (LibException e) {
                        return null;
                    }
                }).collect(Collectors.toList());

        List<Book> result = rawResult.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return new DownloadAllResult(emptyCount.get(), rawResult.size() - result.size(), result);

    }

    private OPDSEntryI checkLinks(OPDSEntryI entry, AtomicInteger emptyCount) {
        long linkCount = entry.getLinks().stream().filter(link -> link.getType().contains(FB2_TYPE)).count();
        if (linkCount > 1) {
            String warning = "Book id: " + entry.getId() + " have more than 1 download link " +
                    "\nBook title:" + entry.getTitle();
            log.warn(warning);
            messengerService.toRole(warning, ZUserService.ADMIN_AUTHORITY);
        } else if (linkCount == 0) {
            emptyCount.incrementAndGet();
        }
        return entry;
    }


    private static class DownloadAllResult {

        private final int empty;
        private final int failed;
        private Collection<Book> success;

        public DownloadAllResult(int empty, int failed, Collection<Book> success) {
            this.empty = empty;
            this.failed = failed;
            this.success = success;
        }

        public int getEmpty() {
            return empty;
        }

        public Collection<Book> getSuccess() {
            return success;
        }

        public int getFailed() {
            return failed;
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

    public void checkSubscriptions() {
        extLibrary.getSubscriptions().forEach(this::checkSubscription);
    }

    private void checkSubscription(Subscription subscription) {
        subscriptionExecutor.submit(() -> {
            Set<String> saved =
                    extLibrary.getSavedBooks().stream().map(SavedBook::getExtId).collect(Collectors.toSet());
            try {
                Stream<OPDSEntryI> newEntries = getAsEntryStream(subscription.getLink()).
                        filter(entry -> !saved.contains(entry.getId()));
                DownloadAllResult result = downloadAll(newEntries, extLibrary::addSaved);

                Optional<String> bookTitles =
                        result.getSuccess().stream().map(Book::getTitle).reduce((s, s2) -> s + ", " + s2);

                messengerService.sendMessageToUser("Subscription result\n " +
                                result.getSuccess().size() + " books was downloaded\n" +
                                bookTitles + "\n" +
                                result.getEmpty() + " wasn't found\n" +
                                result.getFailed() + " failed"
                        , subscription.getUser());
            } catch (LibException e) {
                log.error(e.getMessage(), e);
            }
            extLibService.save(extLibrary);
        });
    }
}
