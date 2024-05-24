package com.time1043.rpceasy.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();
        // 创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        // 监听端口并处理请求
        server.requestHandler(request -> {
            // 处理请求
            System.out.println("Received request: " + request.method() + " " + request.uri());

            // 发送响应
            request.response()
                   .putHeader("content-type", "text/plain")
                   .end("Hello from Vert.x HTTP Server!");
        });

        // 启动 HTTP 服务器 并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}
