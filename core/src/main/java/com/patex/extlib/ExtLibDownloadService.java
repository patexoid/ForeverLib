package com.patex.extlib;


import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.SavedBook;
import com.patex.entities.SavedBookRepository;
import com.patex.entities.ZUser;
import com.patex.messaging.MessengerService;
import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSEntryI;
import com.patex.opds.converters.OPDSLink;
import com.patex.utils.ExecutorCreator;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.patex.extlib.ExtLibService.*;

@Service
public class ExtLibDownloadService {

    private static Logger log = LoggerFactory.getLogger(ExtLibDownloadService.class);

    private final ExecutorService executor = ExecutorCreator.createExecutor("ExtLibDownloadService", log);

    private final ExtLibConnection connection;
    private final ExtLibInScopeRunner scopeRunner;
    private final SavedBookRepository savedBookRepo;
    private final MessengerService messengerService;


    @Autowired
    public ExtLibDownloadService(ExtLibConnection connection, ExtLibInScopeRunner scopeRunner,
                                 SavedBookRepository savedBookRepo, MessengerService messengerService) {
        this.connection = connection;
        this.scopeRunner = scopeRunner;
        this.savedBookRepo = savedBookRepo;

        this.messengerService = messengerService;
    }

    public static Optional<String> extractExtUri(String link) {
        if (link.startsWith("?")) {
            link = link.substring(1);
        }
        Optional<NameValuePair> uriO =
                URLEncodedUtils.parse(link, Charset.forName("UTF-8")).stream().
                        filter(nvp -> nvp.getName().equals(REQUEST_P_NAME)).findFirst();
        return uriO.map(NameValuePair::getValue);
    }

    public Book downloadBook(ExtLibrary library, String uri, String type, ZUser user) {
        Book book = scopeRunner.runInScope(library, () -> connection.downloadBook(uri, type, user));
        savedBookRepo.save(new SavedBook(library, uri));
        return book;
    }

    public ExtLibFeed getExtLibFeed(ExtLibrary library, String uri) throws LibException {
        return scopeRunner.runInScope(library, () -> getExtLibFeed(uri));
    }

    private ExtLibFeed getExtLibFeed(String uri) {
        SyndFeed feed = connection.getFeed(uri);
        List<OPDSEntryI> entries = feed.getEntries().stream().map(ExtLibOPDSEntry::new).
                collect(Collectors.toList());

        List<OPDSLink> links = new ArrayList<>();
        Optional<SyndLink> nextPage = feed.getLinks().stream().
                filter(syndLink -> REL_NEXT.equals(syndLink.getRel())).findFirst();
        nextPage.ifPresent(syndLink -> links.add(ExtLibOPDSEntry.mapLink(syndLink)));
        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    public CompletableFuture<Optional<DownloadAllResult>> downloadAll(ExtLibrary library, String uri, ZUser user) {

        Supplier<Optional<DownloadAllResult>> supplier = () ->
                scopeRunner.runInScope(library, () -> downloadAll(uri, user, library));
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private Optional<DownloadAllResult> downloadAll(String uri, ZUser user, ExtLibrary library) {
        List<OPDSEntryI> entries = getAllEntries(uri);
        Set<String> saved = getAlreadySaved(library, entries);
        Optional<DownloadAllResult> downloadResult = entries.stream().
                filter(entry -> entry.getLinks().stream().map(OPDSLink::getHref).
                        map(ExtLibDownloadService::extractExtUri).
                        filter(Optional::isPresent).map(Optional::get).noneMatch(saved::contains)
                ).map(entry -> download(entry, user, library))
                .reduce(DownloadAllResult::concat);
        downloadResult.ifPresent(result -> messengerService.sendMessageToUser(result, user));
        return downloadResult;
    }

    private List<OPDSEntryI> getAllEntries(String uri) throws LibException {
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

    private Set<String> getAlreadySaved(ExtLibrary library, List<OPDSEntryI> entries) {
        List<String> links = entries.stream().
                map(OPDSEntryI::getLinks).
                flatMap(Collection::stream).map(OPDSLink::getHref).
                map(ExtLibDownloadService::extractExtUri).filter(Optional::isPresent).map(Optional::get).
                distinct().collect(Collectors.toList());
        return savedBookRepo.findSavedBooksByExtLibraryAndExtIdIn(library, links).
                stream().map(SavedBook::getExtId).distinct().collect(Collectors.toSet());
    }

    private DownloadAllResult download(OPDSEntryI entry, ZUser user, ExtLibrary library) {
        List<OPDSLink> links = entry.getLinks().stream().
                filter(link -> link.getType().contains(FB2_TYPE)).collect(Collectors.toList());
        List<String> authors = entry.getAuthors().orElse(Collections.emptyList()).stream().
                map(OPDSAuthor::getName).collect(Collectors.toList());
        if (links.size() == 0) {
            return DownloadAllResult.empty(authors, entry.getTitle());
        } else {
            if (links.size() > 1) {
                log.warn("Book id: " + entry.getId() + " have more than 1 download link " +
                        "\nBook title:" + entry.getTitle());
            }
            try {
                String uri = extractExtUri(links.get(0).getHref()).orElse("");
                String type = "fb2";
                Book book = downloadBook(library, uri, type, user);
                return DownloadAllResult.success(authors, book);
            } catch (LibException e) {
                log.error(e.getMessage(), e);
                return DownloadAllResult.failed(authors, entry.getTitle());
            }
        }
    }
}
