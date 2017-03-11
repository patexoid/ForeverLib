package com.patex.extlib;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.patex.LibException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Base64;
import java.util.concurrent.*;

/**
 *
 */

@Service
public class ExtLibConnectionService {
    private static Logger log = LoggerFactory.getLogger(ExtLibConnectionService.class);
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private final LoadingCache<String, Semaphore> _semaphores = CacheBuilder.newBuilder().build(new CacheLoader<String, Semaphore>() {
        @Override
        public Semaphore load(String key) throws Exception {
            return new Semaphore(2, true);
        }
    });

    public ExtlibConnection openConnection(String urlString) throws LibException {
        return new ExtlibConnection(urlString);
    }

    public class ExtlibConnection {
        private URLConnection _uc;
        private final URL _url;

        private Proxy _proxy;

        private ExtlibConnection(String url) throws LibException {
            try {
                _url = new URL(url);
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
                throw new LibException(e.getMessage(), e);
            }
        }

        private URLConnection getConnection() throws LibException {
            try {
                if (_uc == null) {
                    if (_proxy == null) {
                        _uc = _url.openConnection();
                    } else {
                        _uc = _url.openConnection(_proxy);
                    }
                }
                return _uc;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new LibException(e.getMessage(), e);
            }
        }

        public ExtlibConnection proxy(Proxy.Type proxyType, String proxyHost, int proxyPort) {
            _proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            return this;
        }

        public ExtlibConnection setAuthorization(String login, String password) throws  LibException{
            String userpass = login + ":" + password;
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            getConnection().setRequestProperty("Authorization", basicAuth);
            return this;
        }

        public <E> E getData(ExtLib.ExtLibFunction<URLConnection, E> function) throws LibException {
            try {
                return connectionExecutor.submit(() -> {
                    Semaphore semaphore = _semaphores.get(_url.getHost());
                    semaphore.acquire();
                    try {
                        return function.apply(getConnection());
                    } finally {
                        semaphore.release();
                    }
                }).get(60, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
                throw new LibException(e.getMessage(), e);
            }
        }
    }

    @FunctionalInterface
    public interface ExtLibFunction<T, R> {

        R apply(T t) throws Exception;
    }
}
