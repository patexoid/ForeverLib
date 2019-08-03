package com.patex.storage.server;

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FileDownloadHandler implements RequestHandler {

    private final FileStorage fileStorage;

    @Inject
    public FileDownloadHandler(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public HttpServerRoutes register(HttpServerRoutes routes) {
        return routes.get("/download/{bucket}/{path}", this::download);
    }

    public Publisher download(HttpServerRequest request, HttpServerResponse response) {
        String bucket = request.param("bucket");
        String path = request.param("path");
        return response.sendFile(fileStorage.getPath(bucket, path));
    }
}
