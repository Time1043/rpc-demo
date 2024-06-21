package com.time1043.example.provider;

import com.time1043.example.common.service.UserService;
import com.time1043.rpceasy.registry.LocalRegistry;
import com.time1043.rpceasy.server.HttpServer;
import com.time1043.rpceasy.server.VertxHttpServer;

/**
 * 简单服务提供者示例
 */
public class EasyProviderExample {
    public static void main(String[] args) {
        // 注册服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(8080);
    }
}
