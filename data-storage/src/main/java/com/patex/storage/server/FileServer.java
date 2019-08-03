package com.patex.storage.server;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class FileServer {

    private final int port;

    private FileUploadHandler upload;

    private FileDownloadHandler download;

    @Inject
    public FileServer(@Named("port") String port, FileUploadHandler upload, FileDownloadHandler download) {
        this.port = Integer.parseInt(port);
        this.upload = upload;
        this.download = download;
    }

    public HttpServer startServer() {
        return HttpServer.create()
                .port(port)
                .route(this::registerRoutes);
    }

    private void registerRoutes(HttpServerRoutes routes){
        upload.register(routes);
        download.register(routes);
        routes.get("/upload",(req, res) -> res.sendString(
                        Mono.just("<!DOCTYPE HTML>\n" +
                                "<html>\n" +
                                "<head>\n" +
                                "    <meta charset=\"utf-8\">\n" +
                                "    <title>Тег FORM</title>\n" +
                                "</head>\n" +
                                "<body>\n" +
                                "\n" +
                                "<form action=\"upload/dummy\" enctype=\"multipart/form-data\" method=\"post\">\n" +
                                "     <input type=\"file\" name=\"file\" multiple>\n" +
                                "    <p><input type=\"submit\"></p>\n" +
                                "</form>\n" +
                                "\n" +
                                "</body>\n" +
                                "</html>")
                ));
    }

}
