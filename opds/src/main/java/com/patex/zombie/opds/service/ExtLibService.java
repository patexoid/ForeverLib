package com.patex.zombie.opds.service;

import com.patex.zombie.LibException;
import com.patex.zombie.model.Book;
import com.patex.zombie.model.Res;
import com.patex.zombie.model.User;
import com.patex.zombie.opds.entity.ExtLibrary;
import com.patex.zombie.opds.entity.ExtLibraryRepository;
import com.patex.zombie.opds.entity.SubscriptionEntity;
import com.patex.zombie.opds.model.ExtLibFeed;
import com.patex.zombie.opds.model.OPDSEntryImpl;
import com.patex.zombie.opds.model.converter.ExtLibOPDSEntry;
import com.patex.zombie.opds.model.converter.LinkMapper;
import com.patex.zombie.opds.model.OPDSEntry;
import com.patex.zombie.opds.model.OPDSLink;
import com.patex.zombie.service.ExecutorCreator;
import com.patex.zombie.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.patex.zombie.opds.model.OPDSLink.FB2;
import static com.patex.zombie.opds.model.OPDSLink.OPDS_CATALOG;

/**
 *
 */


@Service
public class ExtLibService {

    public static final String REQUEST_P_NAME = "uri";
    public static final String PARAM_TYPE = "type";
    public static final String NEXT_ID_PREFIX = "next";
    public static final String REL_NEXT = NEXT_ID_PREFIX;
    public static final String FB2_TYPE = "application/fb2";
    public static final String DOWNLOAD_ID_PREFIX = "download";
    public static final String SUBSCRIBE_ID_PREFIX = "subscribe";
    public static final String UNSUBSCRIBE_ID_PREFIX = "unsubscribe";
    private static final Logger log = LoggerFactory.getLogger(ExtLibService.class);
    private final ExtLibDownloadService downloadService;
    private final ExtLibSubscriptionService subscriptionService;
    private final ExtLibraryRepository extLibRepo;
    private final UserService userService;

    private final ExecutorService executor;


    public ExtLibService(ExtLibDownloadService downloadService,
                         ExtLibSubscriptionService subscriptionService,
                         ExtLibraryRepository extLibRepo,
                         UserService userService,
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
        return new OPDSEntryImpl("" + library.getId(), new Res("first.value",
                library.getName()), new OPDSLink("", OPDS_CATALOG));
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
        User user = userService.getCurrentUser();
        Book book = downloadService.downloadBook(library, uri, type, user.getUsername());
        return "/book/loadFile/" + book.getId();
    }

    private void downloadAll(ExtLibrary library, String uri) {
        downloadService.downloadAll(library, uri, userService.getCurrentUser().getUsername());
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
                    OPDSEntry nextEntry = new OPDSEntryImpl(NEXT_ID_PREFIX + ":" + uri, Instant.now(),
                            new Res("opds.extlib.nextPage"),
                            (String) null,
                            nextLink);
                    entries.add(nextEntry);
                });

        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    private List<OPDSEntry> getDownloadAndSubscriptionEntries(ExtLibrary extLibrary, String uri, ExtLibFeed feed) {
        List<OPDSEntry> entries = new ArrayList<>();
        entries.add(new OPDSEntryImpl(DOWNLOAD_ID_PREFIX + ":" + uri, Instant.now(),
                new Res("opds.extlib.download", feed.getTitle()),
                (String) null,
                new OPDSLink(LinkMapper.mapToUri("action/" + Action.downloadAll + "?", uri), OPDS_CATALOG)
        ));


        OPDSEntry subscriptionEntry =
                subscriptionService.find(extLibrary, uri).map(s -> toUnsbscribeEntry(uri, feed, s)).
                        orElseGet(() -> toSubscribeEntry(uri, feed));
        entries.add(subscriptionEntry);
        return entries;
    }

    private OPDSEntryImpl toSubscribeEntry(String uri, ExtLibFeed feed) {
        return new OPDSEntryImpl(SUBSCRIBE_ID_PREFIX + ":" + uri, Instant.now(),
                new Res("opds.extlib.subscribe", feed.getTitle()), (String) null,
                new OPDSLink(LinkMapper.mapToUri("action/" + Action.subscribe + "?", uri),
                        OPDS_CATALOG));
    }

    private OPDSEntryImpl toUnsbscribeEntry(String uri, ExtLibFeed feed, SubscriptionEntity subscription) {
        String id = UNSUBSCRIBE_ID_PREFIX + ":" + uri;
        String href = LinkMapper.mapToUri("action/" + Action.unsubscribe + "?id=" + subscription.getId() + "&", uri);
        return new OPDSEntryImpl(id, Instant.now(),
                new Res("opds.extlib.unsubscribe", feed.getTitle()), (String) null,
                new OPDSLink(href, OPDS_CATALOG));
    }

    private boolean containsDownloadLinks(List<OPDSEntry> entries) {
        return entries.stream().flatMap(entry -> entry.getLinks().stream()).
                anyMatch(link -> link.getType().contains(FB2));
    }

    private void addSubscription(ExtLibrary library, String uri) throws LibException {
        subscriptionService.addSubscription(library, uri);
//        return LinkMapper.mapToUri("?", uri);
    }

    private void deleteSubscription(String idS) throws LibException {
        subscriptionService.deleteSubscription(Long.valueOf(idS));
    }

    public enum Action {
        downloadAll,
        subscribe,
        unsubscribe;
    }
}
