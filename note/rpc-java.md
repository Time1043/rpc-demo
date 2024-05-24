# rpc-java

- Reference

  [yu-rpc (github)](https://github.com/liyupi/yu-rpc), [yupi note](https://wx.zsxq.com/dweb2/index/columns/51122858222824),  

  [Vert.x](https://vertx.io), 
  
  



- Introduction

  轮子类项目，开发RPC框架 

  先核心流程，再做业务扩展

  CRUD，架构设计的思路和技巧



- 几种模式

  RPC

  SDK





## 背景介绍

### PRC概念

- 是什么

  RPC: Remote Procedure Call 远程过程调用

  计算机通信协议

  允许程序在不同的计算机之间进行通信和交互，就像本地调用一样

- 为什么

  `服务消费者` -> `服务提供者`

  不需要了解数据的传输处理过程、底层网络通信的细节 (RPC框架封装)

  开发者可以轻松调用远程服务，快速开发分布式系统



- 例子：A提供者(点餐服务) B消费者

  ```
  interface OrderService {
    // 点餐 返回 orderId
    long order(parameter1, parameter2, parameter3)
  }
  ```

- 没有RPC的调用

  AB独立系统 不能SDK作为依赖包引入

  A 提供web服务 编写点餐接口暴露服务 http://time1043.doc/order
  
  B 服务消费者 构造请求 HttpClient (调用更多第三方服务 麻烦!)

  ```
  url = "http://time1043.doc/order"
  req = new Req(parameter1, parameter2, parameter3)
  resp = httpClient.post(url).body(req).execute()
  orderId = resp.data.orderId
  ```
  
- 有RPC (看起来就像本地调用)

  ```
  orderId = OrderService.order(parameter1, parameter2, parameter3)
  ```

  



### RPC框架实现思路

- 基本设计 (跑通基本调用过程)

  `消费者` -> HttpClient 

  `提供者` <- web服务

- Q：多个服务和方法？单独写接口单独调用 X 

  `请求处理器` 根据客户端的请求参数 调用不同的服务和方法

  `本地服务注册器` 记录服务和对应现实类的映射

  `序列化反序列化` Java对象无法直接在网络中传输

  `代理对象` 简化消费者本地调用

  ![Snipaste_2024-05-24_09-37-48](res/Snipaste_2024-05-24_09-37-48.png)



- 扩展设计

- Q：服务注册发现 (消费者如何知道提供者的调用地址)

  `注册中心` 保存服务提供者的地址 消费者调用服务时从注册中心获取 (`Redis`, `Zookeeper`)

- Q：负载均衡 (多个提供者，消费者该调用谁)

  `负载均衡` 不同算法 (轮询 随机 根据性能动态调用)

- Q：容错机制 (若服务调用失败怎么办)

  `容错机制` 分布式系统的高可用 (失败重试 降级调用其他接口)

  ![Snipaste_2024-05-24_09-43-30](res/Snipaste_2024-05-24_09-43-30.png)

- 更多问题等待优化

  服务提供者下线了怎么办？失效节点剔除机制

  消费者每次都从注册中心拉取信息，性能差吗？使用缓存优化性能

  如何优化RPC框架的传输通讯性能？选择合适的网络框架、自定义协议头、节约传输体积等等

  如果让整个框架更利于扩展？Java的SPI机制、配置化等等

  ...





## 开发简易RPC框架

- 项目初始化

  ```bash
  cd /d/code2/java-code/rpc-oswin/
  mkdir rpc-demo  # IDEA Maven Module 
  # example-common: 示例代码的公共依赖 (包括接口 Model)
  # example-consumer: 示例服务消费者代码
  # example-provider: 示例服务提供者代码
  # rpc-easy: 简易RPC框架
  
  example.common.model.User (class)
  example.common.service.UserService (interface)
  
  
  rpceasy.server.HttpServer (interface)
  rpceasy.server.VertxHttpServer (class)
  
  ```
  
- 项目结构

- `example-common`：需要同时被消费者和提供者引入，主要编写服务相关的接口和数据模型

  用户实体类 User (对象需要序列化接口 为后续网络传输序列化提供支持)

  用户服务接口 UserService

- `example-provider`：实现接口

  导入依赖 

  服务实现类 实现公共模块中定义的用户服务接口

  服务提供者启动类 EasyProviderExample

- `example-consumer`：调用服务

  导入依赖
  
  消费者启动类 EasyConsumerExample
  
- `rpc-easy`：

  VertxHttpServer (能够监听指定端口 并处理请求)





### example-common

- User

  ```java
  package com.time1043.example.common.model;
  
  import java.io.Serializable;
  
  /**
   * 用户
   */
  public class User implements Serializable {
      private String name;
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  }
  
  ```

- UserService

  ```java
  package com.time1043.example.common.service;
  
  import com.time1043.example.common.model.User;
  
  /**
   * 用户服务
   */
  public interface UserService {
      /**
       * 获取用户
       * @param user
       * @return
       */
      User getUser(User user);
  }
  
  ```

  



### example-provider

- 导入依赖

  ```xml
      <dependencies>
          <dependency>
              <groupId>com.time1043</groupId>
              <artifactId>rpc-easy</artifactId>
              <version>1.0-SNAPSHOT</version>
          </dependency>
          <dependency>
              <groupId>com.time1043</groupId>
              <artifactId>example-common</artifactId>
              <version>1.0-SNAPSHOT</version>
          </dependency>
  
          <!-- https://doc.hutool.cn/ -->
          <dependency>
              <groupId>cn.hutool</groupId>
              <artifactId>hutool-all</artifactId>
              <version>5.8.16</version>
          </dependency>
  
          <!-- https://projectlombok.org/ -->
          <dependency>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>1.18.30</version>
              <scope>provided</scope>
          </dependency>
      </dependencies>
  ```

- 服务实现类 UserServiceImpl

  ```java
  package com.time1043.example.provider;
  
  import com.time1043.example.common.model.User;
  import com.time1043.example.common.service.UserService;
  
  public class UserServiceImpl implements UserService {
      @Override
      public User getUser(User user) {
          System.out.println("username: " + user.getName());
          return user;
      }
  }
  
  ```

- 启动类 EasyProviderExample

  ```java
  package com.time1043.example.provider;
  
  /**
   * 简单服务提供者示例
   */
  public class EasyProviderExample {
      public static void main(String[] args) {
          // TODO 提供服务
      }
  }
  
  ```

  



### example-consumer

- 导入依赖

  ```xml
      <dependencies>
          <dependency>
              <groupId>com.time1043</groupId>
              <artifactId>rpc-easy</artifactId>
              <version>1.0-SNAPSHOT</version>
          </dependency>
          <dependency>
              <groupId>com.time1043</groupId>
              <artifactId>example-common</artifactId>
              <version>1.0-SNAPSHOT</version>
          </dependency>
  
          <!-- https://doc.hutool.cn/ -->
          <dependency>
              <groupId>cn.hutool</groupId>
              <artifactId>hutool-all</artifactId>
              <version>5.8.16</version>
          </dependency>
  
          <!-- https://projectlombok.org/ -->
          <dependency>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>1.18.30</version>
              <scope>provided</scope>
          </dependency>
      </dependencies>
  ```

- 启动类

  现在无法获取UserService实例 

  后续会通过RPC框架，快速得到一个支持远程调用服务提供者的代理对象，然后就像调用本地一样调用UserService的方法

  ```java
  package com.time1043.example.consumer;
  
  import com.time1043.example.common.model.User;
  import com.time1043.example.common.service.UserService;
  
  public class EasyConsumerExample {
      public static void main(String[] args) {
          // TODO 需要获取 UserService 的实现类对象
          UserService userService = null;
          User user = new User();
          user.setName("oswin");
  
          // 调用
          User newUser = userService.getUser(user);
          if (newUser!= null){
              System.out.println(newUser.getName());
          } else {
              System.out.println("user == null");
          }
      }
  }
  
  ```

  



### rpc-easy

- web服务器

  让服务提供者 提供 可远程访问的服务

  需要web服务器，能够接受处理请求、并返回响应

- 选择

  SpringBoot内嵌的Tomcat

  NIO框架的Netty和[Vert.x](https://vertx.io)

  

---

- 导入依赖

  ```xml
      <dependencies>
          <!-- https://mvnrepository.com/artifact/io.vertx/vertx-core -->
          <dependency>
              <groupId>io.vertx</groupId>
              <artifactId>vertx-core</artifactId>
              <version>4.5.1</version>
          </dependency>
  
          <!-- https://doc.hutool.cn/ -->
          <dependency>
              <groupId>cn.hutool</groupId>
              <artifactId>hutool-all</artifactId>
              <version>5.8.16</version>
          </dependency>
  
          <!-- https://projectlombok.org/ -->
          <dependency>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>1.18.30</version>
              <scope>provided</scope>
          </dependency>
      </dependencies>
  ```

- web服务器接口HttpServer

  定义统一的启动服务器方法 便于后续扩展 (如实现多种不同的web服务器)

  ```java
  package com.time1043.rpceasy.server;
  
  /**
   * HTTP 服务器接口
   */
  public interface HttpServer {
      /**
       * 启动服务器
       *
       * @param port
       */
      void doStart(int port);
  }
  
  ```

  基于Vert.x实现的web服务器 VertxHttpServer (能够监听指定端口 并处理请求)

  ```java
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
  
  ```

  

---

- 验证web服务器 能够启动成功 并接受请求 EasyProviderExample  http://localhost:8080/

  ```java
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
  
  ```

  





































