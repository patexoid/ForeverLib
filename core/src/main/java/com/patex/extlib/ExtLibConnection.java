package com.patex.extlib;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.patex.LibException;
import com.patex.entities.Book;
import com.patex.entities.ExtLibrary;
import com.patex.entities.ZUser;
import com.patex.opds.OPDSEntry;
import com.patex.opds.OPDSLink;
import com.patex.service.BookService;
import com.patex.utils.ExecutorCreator;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@Component
@Scope(scopeName = "extLibrary", proxyMode = ScopedProxyMode.TARGET_CLASS)
class ExtLibConnection {

    private final static String AUTHORIZATION_PROP_KEY = "Authorization";
    private final static Pattern fileNamePattern = Pattern.compile("attachment; filename=\"([^\"]+)\"");
    private final static Logger log = LoggerFactory.getLogger(ExtLibConnection.class);
    private final Semaphore semaphore = new Semaphore(2);
    private final Cache<String, Book> bookCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    private final ExecutorService executor;
    private final String url;
    private final String prefix;
    private final String login;
    private final String password;
    private final BookService bookService;
    private final Proxy proxy;
    private final int timeout;

    @VisibleForTesting
    ExtLibConnection(String url, String prefix, String login, String password,
                     String proxyHost, Integer proxyPort, Proxy.Type proxyType, ExecutorCreator executorCreator,
                     BookService bookService,
                     int timeout) {
        this.executor = executorCreator.createExecutor("ExtLib:" + url + " Connection", log);
        this.url = url;
        this.prefix = prefix;
        this.login = login;
        this.password = password;
        if (proxyType != null) {
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
        } else {
            proxy = Proxy.NO_PROXY;
        }
        this.bookService = bookService;
        this.timeout = timeout;
    }

    @Autowired
    public ExtLibConnection(BookService bookService, ExtLibScopeStorage extLibScope,
                            @Value("${extlib.connection.timeout}") int timeout, ExecutorCreator executorCreator) {
        this(bookService, extLibScope.getCurrentExtLib(), timeout, executorCreator);
    }

    ExtLibConnection(BookService bookService, ExtLibrary extLibrary, int timeout, ExecutorCreator executorCreator) {
        this(extLibrary.getUrl(), extLibrary.getOpdsPath(), extLibrary.getLogin(), extLibrary.getPassword(),
                extLibrary.getProxyHost(), extLibrary.getProxyPort(), extLibrary.getProxyType(),
                executorCreator,
                bookService, timeout);
    }

    @VisibleForTesting
    URLConnection getConnection(String uri) throws IOException {
        URLConnection connection = new URL(uri).openConnection(proxy);
        if (login != null) {
            String userpass = login + ":" + password;
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            connection.setRequestProperty(AUTHORIZATION_PROP_KEY, basicAuth);
        }
        return connection;
    }

    private <E> E getData(final String uri, ExtLibFunction<URLConnection, E> function) throws LibException {
        try {
            semaphore.acquire();
            return executor.submit(() -> execute(uri, function)
            ).get(timeout, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            if (e.getCause() instanceof LibException) {
                throw (LibException) e.getCause();
            } else {
                throw new LibException(e.getMessage(), e.getCause());
            }
        } finally {
            semaphore.release();
        }
    }

    public Book downloadBook(String uri, String type, ZUser user) {
        try {
            return bookCache.get(uri, () -> getData(uri, conn -> downloadBook(type, conn, user)));
        } catch (ExecutionException | UncheckedExecutionException e) {
            if (e.getCause() instanceof LibException) {
                throw (LibException) e.getCause();
            } else {
                throw new LibException(e.getMessage(), e.getCause());
            }
        }
    }

    private Book downloadBook(String type, URLConnection conn, ZUser user) throws LibException, IOException {
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
        return bookService.uploadBook(fileName, conn.getInputStream(), user);
    }

    public ExtLibFeed getFeed(String uri) throws LibException {
        return getData(uri, this::getFeed);
    }

    private ExtLibFeed getFeed(URLConnection connection) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedInput().build(new XmlReader(connection));
        List<OPDSEntry> entries = feed.getEntries().stream().map(ExtLibOPDSEntry::new).
                collect(Collectors.toList());

        List<OPDSLink> links = feed.getLinks().stream().
                map(LinkMapper::mapLink).filter(Objects::nonNull).
                collect(Collectors.toList());
        return new ExtLibFeed(feed.getTitle(), entries, links);
    }

    private <E> E execute(String uri, ExtLibFunction<URLConnection, E> function) throws Exception {
        return function.apply(getConnection(toUrl(uri)));
    }

    private String toUrl(String uri) {
        if (uri == null) {
            uri = prefix;
            if (!uri.startsWith("/")) {
                uri = "/" + uri;
            }
        }
        return url + uri;
    }
}
