package com.patex.extlib;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.SavedBook;
import com.patex.entities.SavedBookRepository;
import com.patex.entities.Subscription;
import com.patex.entities.SubscriptionRepository;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.opds.OPDSAuthor;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
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

    public static final String PARAM_TYPE = "type";
    static final String REQUEST_P_NAME = "uri";
    static final String FB2_TYPE = "application/fb2";
    static final String REL_NEXT = "next";
    static final String ACTION_DOWNLOAD = "download";
    static final String ACTION_DOWNLOAD_ALL = "downloadAll";
    static final String ACTION_SUBSCRIBE = "subscribe";
    static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    private static Logger log = LoggerFactory.getLogger(ExtLibrary.class);
    private final ExecutorService actionExecutor = Executors.newCachedThreadPool();

    private final Cache<String, Book> bookCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final Pattern fileNamePattern = Pattern.compile("attachment; filename=\"([^\"]+)\"");

    @Autowired
    ZUserService userService;

    private final ExtLibrary extLibrary;
    @Autowired
    private MessengerService messengerService;

    @Autowired
    private SavedBookRepository savedBookRepo;

    @Autowired
    private SubscriptionRepository subscriptionRepo;

    @Autowired
    private BookService bookService;

    @Autowired
    private ExtLibConnection extLibConnectionService;


    public ExtLib(ExtLibrary extLibrary) {
        this.extLibrary = extLibrary;
    }

    private static Optional<String> extractExtUri(String link) {
        if (link.startsWith("?")) {
            link = link.substring(1);
        }
        Optional<NameValuePair> uriO =
                URLEncodedUtils.parse(link, Charset.forName("UTF-8")).stream().
                        filter(nvp -> nvp.getName().equals(REQUEST_P_NAME)).findFirst();
        return uriO.map(NameValuePair::getValue);
    }

    public ExtLibFeed getExtLibFeed(Map<String, String> requestParams) throws LibException {
        String uri = requestParams.get(REQUEST_P_NAME);
        ExtLibFeed feed = getExtLibFeed(uri);
        List<OPDSEntryI> entries = feed.getEntries();
        if (entries.stream().flatMap(entry -> entry.getLinks().stream()).
                anyMatch(link -> link.getType().contains(FB2))) {

            Optional<Subscription> subscription =
                    extLibrary.getSubscriptions().stream().
                            filter(subscriptionf -> uri.equals(subscriptionf.getLink())).findFirst();
            if (subscription.isPresent()) {
                String href = ExtLibOPDSEntry.mapToUri("action/unsubscribe?id=" + subscription.get().getId() + "&", uri);
                entries.add(0, new OPDSEntryImpl("subscribe:" + uri, new Date(),
                        "unsubscribe to " + feed.getTitle(), "Subscribe",
                        new OPDSLink(href, OPDS_CATALOG)));
            } else {
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
        nextPage.ifPresent(syndLink -> links.add(ExtLibOPDSEntry.mapLink(syndLink)));
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
        } else if (ACTION_UNSUBSCRIBE.equals(action)) {
            return deleteSubscription(params.get("id"), params.get(REQUEST_P_NAME));
        }
        throw new LibException("Unknown action: " + action);
    }

    private String deleteSubscription(String idS, String uri) throws LibException {
        Long id = Long.valueOf(idS);
        extLibrary.getSubscriptions().stream().filter(s -> s.getId().equals(id)).findFirst().
                ifPresent(s -> extLibrary.getSubscriptions().remove(s));
        subscriptionRepo.delete(id);
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }

    private String addSubscription(String uri) throws LibException {
        Predicate<Subscription> uriFilter = subscription -> uri.equals(subscription.getLink());
        if (extLibrary.getSubscriptions().stream().
                noneMatch(uriFilter)) {
            ZUser user = userService.getCurrentUser();
            Subscription saved = subscriptionRepo.save(new Subscription(extLibrary, uri, user));
            extLibrary.getSubscriptions().add(saved);
            actionExecutor.execute(() -> checkSubscription(saved));
        }
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }

    private List<OPDSEntryI> getEntries(String uri) throws LibException {
        List<OPDSEntryI> result = new ArrayList<>();
        while (uri != null) {
            String uri0 = uri;
            ExtLibFeed data = getExtLibFeed(uri0);
            result.addAll(data.getEntries());
            Optional<String> nextLink = data.getLinks().stream().
                    filter(link -> REL_NEXT.equals(link.getRel())).
                    findFirst().map(link -> extractExtUri(link.getHref()).orElse(null));
            uri = nextLink.orElse(null);
        }
        return result;
    }

    public void downloadAll(ZUser user, String uri) {
        try {
            downloadAll(getEntries(uri)).ifPresent(
                    result -> messengerService.sendMessageToUser("Download " + result.getResultMessage(), user)
            );
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Optional<DownloadAllResult> downloadAll(List<OPDSEntryI> entryStream) {
        return entryStream.stream().map(this::download).reduce(DownloadAllResult::concat);
    }

    private DownloadAllResult download(OPDSEntryI entry) {
        List<OPDSLink> links = entry.getLinks().stream().
                filter(link -> link.getType().contains(FB2_TYPE)).collect(Collectors.toList());
        List<String> authors = entry.getAuthors().orElse(Collections.emptyList()).stream().
                map(OPDSAuthor::getName).collect(Collectors.toList());
        if (links.size() > 1) {
            String warning = "Book id: " + entry.getId() + " have more than 1 download link " +
                    "\nBook title:" + entry.getTitle();
            log.warn(warning);
            messengerService.toRole(warning, ZUserService.ADMIN_AUTHORITY);
            return DownloadAllResult.empty(authors, entry.getTitle());
        } else if (links.size() == 0) {
            return DownloadAllResult.empty(authors, entry.getTitle());
        } else {
            try {
                Book book = downloadFromExtLib("fb2", extractExtUri(links.get(0).getHref()).orElse(""));
                return DownloadAllResult.success(authors, book);
            } catch (LibException e) {
                log.error(e.getMessage(), e);
                return DownloadAllResult.failed(authors, entry.getTitle());
            }
        }
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
                    Book book = bookService.uploadBook(fileName, conn.getInputStream());
                    savedBookRepo.save(new SavedBook(extLibrary, uri));
                    return book;
                })
        );
    }

    public void checkSubscriptions() {
        actionExecutor.submit(() -> extLibrary.getSubscriptions().forEach(this::checkSubscription));
    }

    private void checkSubscription(Subscription subscription) {
        try {
            List<OPDSEntryI> entries = getEntries(subscription.getLink());
            List<String> links = entries.stream().
                    map(OPDSEntryI::getLinks).
                    flatMap(Collection::stream).map(OPDSLink::getHref).
                    map(ExtLib::extractExtUri).filter(Optional::isPresent).map(Optional::get).
                    distinct().collect(Collectors.toList());
            Set<String> saved = savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(extLibrary, links).
                    stream().map(SavedBook::getExtId).distinct().collect(Collectors.toSet());

            List<OPDSEntryI> newEntries = entries.stream().
                    filter(entry -> entry.getLinks().stream().
                            map(link -> extractExtUri(link.getHref())).
                            filter(Optional::isPresent).map(Optional::get).noneMatch(saved::contains)
                    ).collect(Collectors.toList());

            downloadAll(newEntries).
                    filter(result -> result.success.size() > 0 || result.failed.size() > 0).
                    ifPresent(
                            result -> messengerService.
                                    sendMessageToUser("Subscription " + result.getResultMessage(), subscription.getUser()));
        } catch (LibException e) {
            log.error(e.getMessage(), e);
        }
    }

    public OPDSEntryI getRootEntry() {
        return new OPDSEntryImpl("" + extLibrary.getId(), extLibrary.getName(), new OPDSLink("", OPDS_CATALOG));
    }

    public String getExtLibId() {
        return "" + extLibrary.getId();
    }

    private static class DownloadAllResult {

        private final List<String> authors;
        private final List<String> emptyBooks;
        private final List<String> failed;
        private final List<Book> success;

        public DownloadAllResult(List<String> authors, List<String> emptyBooks, List<String> failed, List<Book> success) {
            this.authors = authors;
            this.emptyBooks = emptyBooks;
            this.failed = failed;
            this.success = success;
        }

        public static DownloadAllResult empty(List<String> authors, String empty) {
            return new DownloadAllResult(authors,
                    Collections.singletonList(empty), Collections.emptyList(), Collections.emptyList());
        }

        public static DownloadAllResult failed(List<String> authors, String failed) {
            return new DownloadAllResult(authors,
                    Collections.emptyList(), Collections.singletonList(failed), Collections.emptyList());
        }

        public static DownloadAllResult success(List<String> authors, Book success) {
            return new DownloadAllResult(authors,
                    Collections.emptyList(), Collections.emptyList(), Collections.singletonList(success));
        }

        public DownloadAllResult concat(DownloadAllResult other) {
            List<String> authors = new ArrayList<>(this.authors);
            authors.addAll(other.authors);
            ArrayList<String> emptyBooks = new ArrayList<>(this.emptyBooks);
            emptyBooks.addAll(other.emptyBooks);
            ArrayList<String> failed = new ArrayList<>(this.failed);
            failed.addAll(other.failed);
            ArrayList<Book> success = new ArrayList<>(this.success);
            success.addAll(other.success);
            return new DownloadAllResult(authors, emptyBooks, failed, success);
        }

        public String getResultMessage() {

            BinaryOperator<String> concat = (s, s2) -> s + ", " + s2;
            return "result\n" +
                    "Authors:\n" +
                    authors.stream().collect(Collectors.groupingBy(o -> o)).
                            entrySet().stream().sorted(Comparator.comparingInt(o -> -o.getValue().size())).
                            limit(5).map(Map.Entry::getKey).reduce(concat).orElse("") + "\n" +
                    "\n" +
                    "Success:\n" +
                    success.stream().map(Book::getTitle).reduce(concat).orElse("") + "\n" +
                    "\n" +
                    "Empty:\n" +
                    emptyBooks.stream().reduce(concat).orElse("") + "\n" +
                    "\n" +
                    "Failed:\n" +
                    failed.stream().reduce(concat).orElse("") + "\n";


        }
    }
}
