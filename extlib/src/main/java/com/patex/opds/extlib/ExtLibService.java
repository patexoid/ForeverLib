package com.patex.opds.extlib;

import com.patex.LibException;
import com.patex.zombie.core.entities.*;
import com.patex.opds.OPDSEntry;
import com.patex.opds.OPDSLink;
import com.patex.zombie.core.service.ZUserService;
import com.patex.zombie.core.utils.ExecutorCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.patex.opds.OPDSLink.FB2;

/**
 *
 */


@Service
public class ExtLibService {

    public static final String REQUEST_P_NAME = "uri";
    public static final String PARAM_TYPE = "type";
    public static final String NEXT_ID_PREFIX = "next";
    static final String REL_NEXT = NEXT_ID_PREFIX;
    static final String FB2_TYPE = "application/fb2";
    static final String DOWNLOAD_ID_PREFIX = "download";
    static final String SUBSCRIBE_ID_PREFIX = "subscribe";
    static final String UNSUBSCRIBE_ID_PREFIX = "unsubscribe";
    private static final Logger log = LoggerFactory.getLogger(ExtLibService.class);
    private final ExtLibDownloadService downloadService;
    private final ExtLibSubscriptionService subscriptionService;
    private final ExtLibraryRepository extLibRepo;
    private final ZUserService userService;

    private final ExecutorService executor;


    public ExtLibService(ExtLibDownloadService downloadService,
                         ExtLibSubscriptionService subscriptionService,
                         ExtLibraryRepository extLibRepo,
                         ZUserService userService,
                         ExecutorCreator executorCreator) {
        this.downloadService = downloadService;
        this.subscriptionService = subscriptionService;
        this.extLibRepo = extLibRepo;
        this.userService = userService;
        executor = executorCreator.createExecutor("ExtLibService", log);
    }

    public List<OPDSEntry> getRoot(String prefix) {
        return extLibRepo.findAll().stream().
                map(extLib -> new ExtLibOPDSEntry(getRootEntry(extLib), prefix + "/" + extLib.getId()))
                .collect(Collectors.toList());
    }

    private OPDSEntry getRootEntry(ExtLibrary library) {
        return OPDSEntry.builder("" + library.getId(), "first.value", library.getName()).addLink("").build();
    }

    public void actionExtLibData(long libId, String action, Map<String, String> params) throws LibException {
        ExtLibrary library = extLibRepo.findById(libId).orElse(null);
        action(library, action, params, params.get(REQUEST_P_NAME));
    }

    private void action(ExtLibrary library,
                        String actionS, Map<String, String> params, String uri) throws LibException {
        Action action = Action.valueOf(actionS);
        if (Action.downloadAll.equals(action)) {
            downloadAll(library, uri);
        } else if (Action.subscribe.equals(action)) {
            addSubscription(library, uri);
        } else if (Action.unsubscribe.equals(action)) {
            deleteSubscription(params.get("id"));
        } else {
            throw new LibException("Unknown action: " + action);
        }
    }

    public String downloadBook(long libId, String uri, String type) throws LibException {
        Optional<ExtLibrary> library = extLibRepo.findById(libId);
        return library.map(l -> downloadBook(l, uri, type)).orElseThrow(() -> new LibException("Unknown Library"));
    }

    private String downloadBook(ExtLibrary library, String uri, String type) throws LibException {
        ZUser user = userService.getCurrentUser();
        Book book = downloadService.downloadBook(library, uri, type, user);
        return "/book/loadFile/" + book.getId();
    }

    private void downloadAll(ExtLibrary library, String uri) {
        downloadService.downloadAll(library, uri, userService.getCurrentUser());
//        return LinkMapper.mapToUri("?", uri);
    }

    @Scheduled(cron = "0 00 12 * * *")
    public void checkSubscriptions() {
        executor.execute(() ->
                extLibRepo.findAll().forEach(subscriptionService::checkSubscriptions));
    }

    public ExtLibFeed getDataForLibrary(Long libId, Map<String, String> requestParams)
            throws LibException {
        ExtLibrary library = extLibRepo.findById(libId).get();
        String uri = requestParams.get(REQUEST_P_NAME);
        return getExtLibFeed(library, uri);
    }

    private ExtLibFeed getExtLibFeed(ExtLibrary extLibrary, String uri) throws LibException {
        ExtLibFeed feed = downloadService.getExtLibFeed(extLibrary, uri);
        List<OPDSEntry> entries = new ArrayList<>(feed.getEntries());
        if (containsDownloadLinks(entries)) {
            entries.addAll(0, getDownloadAndSubscriptionEntries(extLibrary, uri, feed));
        }
        List<OPDSLink> links = feed.getLinks();
        feed.getLinks().stream().
                filter(link -> REL_NEXT.equals(link.getRel())).
                findFirst().
                ifPresent(nextLink -> {
                    entries.add(
                            OPDSEntry.builder(NEXT_ID_PREFIX + ":" + uri, "opds.extlib.nextPage").
                                    addLink(nextLink).build());
                });

        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    private List<OPDSEntry> getDownloadAndSubscriptionEntries(ExtLibrary extLibrary, String uri, ExtLibFeed feed) {
        List<OPDSEntry> entries = new ArrayList<>();

        entries.add(
                OPDSEntry.builder(DOWNLOAD_ID_PREFIX + ":" + uri, "opds.extlib.download", feed.getTitle()).
                        addLink(LinkMapper.mapToUri("action/" + Action.downloadAll + "?", uri)).
                        build());

        OPDSEntry subscriptionEntry =
                subscriptionService.find(extLibrary, uri).map(s -> toUnsbscribeEntry(uri, feed, s)).
                        orElseGet(() -> toSubscribeEntry(uri, feed));
        entries.add(subscriptionEntry);
        return entries;
    }

    private OPDSEntry toSubscribeEntry(String uri, ExtLibFeed feed) {
        return OPDSEntry.builder(SUBSCRIBE_ID_PREFIX + ":" + uri, "opds.extlib.subscribe", feed.getTitle()).
                addLink(LinkMapper.mapToUri("action/" + Action.subscribe + "?", uri)).build();
    }

    private OPDSEntry toUnsbscribeEntry(String uri, ExtLibFeed feed, Subscription subscription) {
        return OPDSEntry.builder(UNSUBSCRIBE_ID_PREFIX + ":" + uri, "opds.extlib.unsubscribe", feed.getTitle()).
                addLink(LinkMapper.mapToUri("action/" + Action.unsubscribe + "?id=" + subscription.getId() + "&", uri)).build();
    }

    private boolean containsDownloadLinks(List<OPDSEntry> entries) {
        return entries.stream().flatMap(entry -> entry.getLinks().stream()).
                anyMatch(link -> link.getType().contains(FB2));
    }

    private void addSubscription(ExtLibrary library, String uri) throws LibException {
        subscriptionService.addSubscription(library, uri);
    }

    private void deleteSubscription(String idS) throws LibException {
        subscriptionService.deleteSubscription(Long.valueOf(idS));
    }

    enum Action {
        downloadAll,
        subscribe,
        unsubscribe;
    }
}
