package com.patex.storage.server;

import reactor.netty.http.server.HttpServerRoutes;

public interface RequestHandler {

     HttpServerRoutes register(HttpServerRoutes routes);
}
