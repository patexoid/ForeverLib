package com.patex.extlib;


import com.patex.LibException;
import com.patex.entities.*;
import com.patex.messaging.MessengerService;
import com.patex.opds.converters.OPDSAuthor;
import com.patex.opds.converters.OPDSEntry;
import com.patex.opds.converters.OPDSLink;
import com.patex.service.TransactionService;
import com.patex.utils.ExecutorCreator;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.patex.extlib.ExtLibService.FB2_TYPE;
import static com.patex.extlib.ExtLibService.REL_NEXT;
import static com.patex.extlib.ExtLibService.REQUEST_P_NAME;

@Service
public class ExtLibDownloadService {

    private static final Logger log = LoggerFactory.getLogger(ExtLibDownloadService.class);


    private final ExtLibConnection connection;
    private final ExtLibInScopeRunner scopeRunner;
    private final SavedBookRepository savedBookRepo;
    private final MessengerService messengerService;
    private final ExecutorService executor;
    private final TransactionService transactionService;

    @Autowired
    public ExtLibDownloadService(ExtLibConnection connection,
                                 ExtLibInScopeRunner scopeRunner,
                                 SavedBookRepository savedBookRepo,
                                 MessengerService messengerService,
                                 ExecutorCreator executorCreator,
                                 TransactionService transactionService) {
        this.connection = connection;
        this.scopeRunner = scopeRunner;
        this.savedBookRepo = savedBookRepo;
        this.messengerService = messengerService;
        this.executor = executorCreator.createExecutor("ExtLibDownloadService", log);
        this.transactionService = transactionService;
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

    public Book downloadBook(ExtLibrary library, String uri, String type, ZUser user)  throws LibException{
        return transactionService.transactionRequired(() -> {
            SavedBook savedInfo =
                    savedBookRepo.findSavedBooksByExtLibraryAndExtId(library, uri).orElse(new SavedBook(library, uri));
            try {
                Book book = scopeRunner.runInScope(library, () -> connection.downloadBook(uri, type, user));
                savedInfo.success();
                savedBookRepo.save(savedInfo);
                return book;
            } catch (LibException e) {
                savedInfo.failed();
                savedBookRepo.save(savedInfo);
                throw e;
            }
        });
    }

    public ExtLibFeed getExtLibFeed(ExtLibrary library, String uri) throws LibException {
        return scopeRunner.runInScope(library, () -> getExtLibFeed(uri));
    }

    private ExtLibFeed getExtLibFeed(String uri) {
        return connection.getFeed(uri);
    }

    public CompletableFuture<Optional<DownloadAllResult>> downloadAll(ExtLibrary library, String uri, ZUser user) {

        Supplier<Optional<DownloadAllResult>> supplier = () ->
                scopeRunner.runInScope(library, () -> downloadAll(uri, user, library));
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private Optional<DownloadAllResult> downloadAll(String uri, ZUser user, ExtLibrary library) {
        List<OPDSEntry> entries = getAllEntries(uri);
        Set<String> saved = getAlreadySaved(library, entries);
        Optional<DownloadAllResult> downloadResult = entries.stream().
                filter(entry -> isSaved(saved, entry)).
                map(entry -> download(entry, user, library))
                .reduce(DownloadAllResult::concat);
        downloadResult.
                filter(DownloadAllResult::hasResult).
                ifPresent(result -> messengerService.sendMessageToUser(result, user));
        return downloadResult;
    }

    private boolean isSaved(Set<String> saved, OPDSEntry entry) {
        return entry.getLinks().stream().
                map(OPDSLink::getHref).
                map(ExtLibDownloadService::extractExtUri).
                filter(Optional::isPresent).
                map(Optional::get).
                noneMatch(saved::contains);
    }

    private List<OPDSEntry> getAllEntries(String uri) throws LibException {
        List<OPDSEntry> result = new ArrayList<>();
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

    private Set<String> getAlreadySaved(ExtLibrary library, List<OPDSEntry> entries) {
        List<String> links = entries.stream().
                map(OPDSEntry::getLinks).
                flatMap(Collection::stream).map(OPDSLink::getHref).
                map(ExtLibDownloadService::extractExtUri).filter(Optional::isPresent).map(Optional::get).
                distinct().collect(Collectors.toList());
        return savedBookRepo.
                findSavedBooksByExtLibraryAndFailedDownloadCountLessThanAndExtIdIn(library, 5, links).
                stream().map(SavedBook::getExtId).collect(Collectors.toSet());
    }

    private DownloadAllResult download(OPDSEntry entry, ZUser user, ExtLibrary library) {
        List<OPDSLink> links = entry.getLinks().stream().
                filter(link -> link.getType().contains(FB2_TYPE)).
                collect(Collectors.toList());
        List<String> authors = entry.getAuthors().stream().
                map(OPDSAuthor::getName).collect(Collectors.toList());
        if (links.size() == 0) {
            return DownloadAllResult.empty(authors, entry.getTitle());
        } else {
            if (links.size() > 1) {
                log.warn("Book id: " + entry.getId() + " have more than 1 download link " +
                        "\nBook title:" + entry.getTitle());
            }
            try {
                OPDSLink link = links.get(0);
                String uri = extractExtUri(link.getHref()).orElse("");
                String type = link.getType();
                if (type.contains("/")) {
                    type = type.substring(type.lastIndexOf("/") + 1);
                }
                Book book = downloadBook(library, uri, type, user);
                return DownloadAllResult.success(authors, book);
            } catch (LibException e) {
                log.warn(e.getMessage(), e);
                return DownloadAllResult.failed(authors, entry.getTitle());
            }
        }
    }
}
