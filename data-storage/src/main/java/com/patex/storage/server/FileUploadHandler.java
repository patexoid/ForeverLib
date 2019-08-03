package com.patex.storage.server;

import com.patex.LibException;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class FileUploadHandler implements RequestHandler {

    private final FileStorage fileStorage;

    @Inject
    public FileUploadHandler(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    @Override
    public HttpServerRoutes register(HttpServerRoutes routes) {
        return routes.post("/upload/{path}", this::upload);
    }

    public Publisher upload(HttpServerRequest request, HttpServerResponse response) {
        return response.sendString(
                uploadFile(request));
    }

    private Publisher<String> uploadFile(HttpServerRequest request) {
        return request.receive()
                .aggregate()
                .flatMapMany(byteBuf -> {
                    FullHttpRequest dhr = new DefaultFullHttpRequest(request.version(), request.method(), request.uri(), byteBuf, request.requestHeaders(), EmptyHttpHeaders.INSTANCE);
                    HttpPostMultipartRequestDecoder postDecoder = new HttpPostMultipartRequestDecoder(new DefaultHttpDataFactory(false), dhr);
                    String path = request.param("path");
                    return Flux.fromIterable(postDecoder.getBodyHttpDatas())
                            .filter(data -> data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload)
                            .cast(FileUpload.class)
                            .map(fu -> fileStorage.save(getFileContent(fu), path, fu.getFilename()))
                            .doFinally(signalType -> {
                                postDecoder.destroy();
                                dhr.release();
                            });
                }).flatMap(s -> Flux.just(s, "\n"));
    }

    private byte[] getFileContent(FileUpload fu) throws LibException {
        try {
            return fu.get();
        } catch (IOException e) {
            throw new LibException(e);
        }
    }
}
