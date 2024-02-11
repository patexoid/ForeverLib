package com.patex.forever.opds.service;


import com.patex.forever.messaging.MessengerService;
import com.patex.forever.LibException;
import com.patex.forever.model.Book;
import com.patex.forever.opds.entity.ExtLibrary;
import com.patex.forever.opds.entity.SavedBook;
import com.patex.forever.opds.entity.SavedBookRepository;
import com.patex.forever.opds.model.DownloadAllResult;
import com.patex.forever.opds.model.ExtLibFeed;
import com.patex.forever.opds.model.converter.OPDSAuthor;
import com.patex.forever.opds.model.OPDSEntry;
import com.patex.forever.opds.model.OPDSLink;
import com.patex.forever.service.ExecutorCreator;
import com.patex.forever.service.TransactionService;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.patex.forever.opds.service.ExtLibService.*;

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

    public Book downloadBook(ExtLibrary library, String uri, String type, String user) throws LibException {
        return transactionService.transactionRequired(() -> {
            SavedBook savedInfo =
                    savedBookRepo.findSavedBooksByExtLibraryAndExtId(library, uri).
                            orElseGet(() -> new SavedBook(library, uri));
            try {
                Book book = scopeRunner.runInScope(library, () -> connection.downloadBook(uri, type, user));
                savedInfo.success();
                savedBookRepo.save(savedInfo);
                return book;
            } catch (LibException e) {
                savedInfo.failed();
                transactionService.newTransaction(() -> savedBookRepo.save(savedInfo));

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

    public CompletableFuture<Optional<DownloadAllResult>> downloadAll(ExtLibrary library, String uri, String user) {

        Supplier<Optional<DownloadAllResult>> supplier = () ->
                scopeRunner.runInScope(library, () -> downloadAll(uri, user, library));
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    private Optional<DownloadAllResult> downloadAll(String uri, String user, ExtLibrary library) {
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
                findSavedBooksByExtLibraryAndExtIdIn(library, links).stream().
                filter(sb -> sb.getFailedDownloadCount() > 5 || sb.getFailedDownloadCount() == 0).
                map(SavedBook::getExtId).collect(Collectors.toSet());
    }

    private DownloadAllResult download(OPDSEntry entry, String user, ExtLibrary library) {
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
                type = type.replace("+",".");
                Book book = downloadBook(library, uri, type, user);
                return DownloadAllResult.success(authors, book);
            } catch (LibException e) {
                log.warn(e.getMessage(), e);
                return DownloadAllResult.failed(authors, entry.getTitle());
            }
        }
    }
}
