package com.time1043.example.provider;

import com.time1043.rpceasy.server.HttpServer;
import com.time1043.rpceasy.server.VertxHttpServer;

/**
 * 简单服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
