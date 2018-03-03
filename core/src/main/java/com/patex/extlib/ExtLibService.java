package com.patex.extlib;

import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ExtLibraryRepository;
import com.patex.entities.Subscription;
import com.patex.entities.ZUser;
import com.patex.opds.converters.OPDSEntryI;
import com.patex.opds.converters.OPDSEntryImpl;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.ZUserService;
import com.patex.utils.ExecutorCreator;
import com.patex.utils.Res;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.patex.opds.converters.OPDSLink.FB2;
import static com.patex.opds.converters.OPDSLink.OPDS_CATALOG;

/**
 *
 */


@Service
public class ExtLibService {

    static final String REQUEST_P_NAME = "uri";
    static final String REL_NEXT = "next";
    static final String FB2_TYPE = "application/fb2";
    private static final String PARAM_TYPE = "type";
    private static final String ACTION_DOWNLOAD = "download";
    private static final String ACTION_DOWNLOAD_ALL = "downloadAll";
    private static final String ACTION_SUBSCRIBE = "subscribe";
    private static final String ACTION_UNSUBSCRIBE = "unsubscribe";
    private static Logger log = LoggerFactory.getLogger(ExtLibService.class);

    private final ExtLibDownloadService downloadService;
    private final ExtLibSubscriptionService subscriptionService;
    private ExtLibraryRepository extLibRepo;

    private ExecutorService executor = ExecutorCreator.createExecutor("ExtLibService", log);

    public ExtLibService(ExtLibDownloadService downloadService, ExtLibSubscriptionService subscriptionService,
                         ExtLibraryRepository extLibRepo) {
        this.downloadService = downloadService;
        this.subscriptionService = subscriptionService;
        this.extLibRepo = extLibRepo;
    }

    @Autowired
    private ZUserService userService;

    public List<OPDSEntryI> getRoot(String prefix) {
        return extLibRepo.findAll().stream().
                map(extLib -> new ExtLibOPDSEntry(getRootEntry(extLib), prefix + "/" + extLib.getId()))
                .collect(Collectors.toList());
    }

    private OPDSEntryI getRootEntry(ExtLibrary library) {
        return new OPDSEntryImpl("" + library.getId(), new Res("opds.first.value",
                library.getName()), new OPDSLink("", OPDS_CATALOG));
    }

    public String actionExtLibData(long libId, String action, Map<String, String> params) throws LibException {
        ExtLibrary library = extLibRepo.findOne(libId);
        return action(library, action, params);
    }

    private String action(ExtLibrary library,
                          String action, Map<String, String> params) throws LibException {
        if (ACTION_DOWNLOAD.equals(action)) {
            return downloadBook(library, params);
        } else if (ACTION_DOWNLOAD_ALL.equals(action)) {
            return downloadAll(library, params);
        } else if (ACTION_SUBSCRIBE.equals(action)) {
            return addSubscription(library, params);
        } else if (ACTION_UNSUBSCRIBE.equals(action)) {
            return deleteSubscription(params.get("id"), params.get(REQUEST_P_NAME));
        }
        throw new LibException("Unknown action: " + action);
    }

    private String downloadBook(ExtLibrary library, Map<String, String> params) throws LibException {
        String uri = params.get(REQUEST_P_NAME);
        String type = params.get(PARAM_TYPE);
        ZUser user = userService.getCurrentUser();
        Book book = downloadService.downloadBook(library, uri, type, user);
        return "/book/loadFile/" + book.getId();
    }

    private String downloadAll(ExtLibrary library, Map<String, String> params) {
        String uri = params.get(REQUEST_P_NAME);
        downloadService.downloadAll(library, uri, userService.getCurrentUser());
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }

    @Scheduled(cron = "0 00 12 * * *")
    public void checkSubscriptions() {
        executor.execute(() ->
                extLibRepo.findAll().forEach(subscriptionService::checkSubscriptions));
    }

    public ExtLibFeed getDataForLibrary(Long libId, Map<String, String> requestParams)
            throws LibException {
        ExtLibrary library = extLibRepo.findOne(libId);
        String uri = requestParams.get(REQUEST_P_NAME);
        return getExtLibFeed(library, uri);
    }

    private ExtLibFeed getExtLibFeed(ExtLibrary extLibrary, String uri) throws LibException {
        ExtLibFeed feed = downloadService.getExtLibFeed(extLibrary, uri);
        List<OPDSEntryI> entries = feed.getEntries();
        if (containsDownloadLinks(entries)) {
            entries.addAll(0, getDownloadAndSubscriptionEntries(extLibrary, uri, feed));
        }
        List<OPDSLink> links = feed.getLinks();
        feed.getLinks().stream().
                filter(link -> REL_NEXT.equals(link.getRel())).
                findFirst().
                ifPresent(nextLink -> {
                    OPDSEntryI nextEntry = new OPDSEntryImpl("next:" + uri, new Date(),
                            new Res("opds.extlib.nextPage"),
                            null,
                            nextLink);
                    entries.add(nextEntry);
                });

        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    private List<OPDSEntryI> getDownloadAndSubscriptionEntries(ExtLibrary extLibrary, String uri, ExtLibFeed feed) {
        List<OPDSEntryI> entries = new ArrayList<>();
        entries.add(new OPDSEntryImpl("download:" + uri, new Date(),
                new Res("opds.extlib.download", feed.getTitle()),
                null,
                new OPDSLink(ExtLibOPDSEntry.mapToUri("action/downloadAll?", uri), OPDS_CATALOG)
        ));
        OPDSEntryI subscriptionEntry =
                extLibrary.getSubscriptions().stream().
                        filter(s -> uri.equals(s.getLink())).
                        findFirst().map(s -> toUnsbscribeEntry(uri, feed, s)).
                        orElseGet(() -> toSubscribeEntry(uri, feed));
        entries.add(subscriptionEntry);
        return entries;
    }

    private OPDSEntryImpl toSubscribeEntry(String uri, ExtLibFeed feed) {
        return new OPDSEntryImpl("subscribe:" + uri, new Date(),
                new Res("opds.extlib.subscribe", feed.getTitle()), null,
                new OPDSLink(ExtLibOPDSEntry.mapToUri("action/subscribe?", uri),
                        OPDS_CATALOG));
    }

    private OPDSEntryImpl toUnsbscribeEntry(String uri, ExtLibFeed feed, Subscription subscription) {
        String id = "unsubscribe:" + uri;
        String href = ExtLibOPDSEntry.mapToUri("action/unsubscribe?id=" + subscription.getId() + "&", uri);
        return new OPDSEntryImpl(id, new Date(),
                new Res("opds.extlib.unsubscribe", feed.getTitle()), null,
                new OPDSLink(href, OPDS_CATALOG));
    }

    private boolean containsDownloadLinks(List<OPDSEntryI> entries) {
        return entries.stream().flatMap(entry -> entry.getLinks().stream()).
                anyMatch(link -> link.getType().contains(FB2));
    }

    private String addSubscription(ExtLibrary library, Map<String, String> params) throws LibException {
        String uri = params.get(REQUEST_P_NAME);
        subscriptionService.addSubscription(library, uri);
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }

    private String deleteSubscription(String idS, String uri) throws LibException {
        subscriptionService.deleteSubscription(Long.valueOf(idS));
        return ExtLibOPDSEntry.mapToUri("?", uri);
    }
}
