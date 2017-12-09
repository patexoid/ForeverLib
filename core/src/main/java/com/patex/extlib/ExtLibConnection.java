package com.patex.extlib;

import com.google.common.annotations.VisibleForTesting;
import com.patex.LibException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 */

class ExtLibConnection {

    private static Logger log = LoggerFactory.getLogger(ExtLibConnection.class);

    private final ExecutorService executor;
    private final String url;
    private final String prefix;
    private final String login;
    private final String password;
    private final Proxy proxy;
    private final Semaphore semaphore = new Semaphore(2);


    ExtLibConnection(String url, String prefix, String login, String password,
                     String proxyHost, Integer proxyPort, Proxy.Type proxyType, ExecutorService executor) {
        this.url = url;
        this.prefix = prefix;
        this.login = login;
        this.password = password;
        this.executor = executor;
        if (proxyType != null) {
            proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
        } else {
            proxy = Proxy.NO_PROXY;
        }
    }

    @VisibleForTesting
    URLConnection getConnection(String uri) throws IOException {
        URLConnection connection = new URL(uri).openConnection(proxy);
        if (login != null) {
            String userpass = login + ":" + password;
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            connection.setRequestProperty("Authorization", basicAuth);
        }
        return connection;
    }

    public <E> E getData(final String uri, ExtLibFunction<URLConnection, E> function) throws LibException {
        try {
            semaphore.acquire();
            return executor.submit(() -> execute(uri, function)
            ).get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new LibException(e.getMessage(), e);
        } finally {
            semaphore.release();
        }
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
