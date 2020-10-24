# 这是对**TIANAI-RPC** 框架的**springboot**脚手架工具，方便配置启动

## 使用方式

### 	 1. 在Springboot项目中引入依赖

```xml
<dependency>
    <groupId>cloud.tianai.rpc</groupId>
    <artifactId>tianai-rpc-springboot-starter</artifactId>
    <version>1.3.7</version>
</dependency>
```

### 2. 相关配置

> 配置服务注册、序列化、断开等等

- server端

```yaml
tianai-rpc:
  # 服务注册， 默认 zookeeper
  registry: 
    name: zookeeper # 支持zookeeper和nacos
    # 服务注册的地址， 默认 127.0.0.1:2181
    address: 127.0.0.1:2181 # 地址
    # otterProp: 配置nacos一些其他配置时这样配置，比如配置namespace 
     #  namespace: xxx
  # 序列化 默认hessian2
  codec: hessian2
  server:
    # server端启动时的端口， 默认20881
    port: 20883
    # 超时，单位 ms
    timeout: 5000
```

- client端
```yaml
tianai-rpc:
  registry: 
    # 指定服务注册 为nacos，默认zookeeper
    name: nacos
    # 指定注册地址 默认 127.0.0.1:2181
    address: 127.0.0.1:8848
    otterProp:
      # 配置nacos命名空间
      namespace: 1ca3c65a-92a7-4a09-8de1-4bfe1c89d240
  # 指定序列化，默认hessian2
  codec: hessian2

```
---

### 3. 使用(注解方式): 

- server端

> 
>要提供RPC服务的类上加上 **@RpcProvider**注解即可
 ```java
     @Service
     @RpcProvider
     public class DemoServiceImpl implements DemoService {
         
     }
 ```
- client端

>要使用RPC服务的接口的field中加上 **@RpcConsumer** 注解即可
 ```java
 @RestController
 public class DemoController {
 
     @RpcConsumer
     private DemoService demoService;
 
     @GetMapping("/port")
     public Integer getPort() {
         demoService.toString();
         return demoService.getPort();
     }
 }
 ```

### 4. xml 方式

- server端

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:rpc="http://rpc.tianai.cloud/schema/rpc"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd
      http://rpc.tianai.cloud/schema/rpc
      http://rpc.tianai.cloud/schema/rpc/rpc.xsd">
      <!-- 指定要暴露的服务 -->
      <rpc:provider interface="com.example.service.DemoService" ref="demoServiceImpl" weight="300">
      </rpc:provider>
  
  </beans>
  ```

- client端

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <beans xmlns="http://www.springframework.org/schema/beans"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns:rpc="http://rpc.tianai.cloud/schema/rpc"
         xsi:schemaLocation="http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd
      http://rpc.tianai.cloud/schema/rpc
      http://rpc.tianai.cloud/schema/rpc/rpc.xsd">
  	<!-- 指定要调用的rpc服务的接口 -->
      <rpc:consumer interface="com.example.service.DemoService" id="demoService"/>
  </beans>
  ```

- 将配置好的xml注入到spring中 

  例如: `@ImportResource(locations = "classpath:rpc.xml")`

- client直接使用原生spring方式注入接口就可以使用了

  例如: 

  ```java
   @RestController
   public class DemoController {
   
       @Autowired
       private DemoService demoService;
   
       @GetMapping("/port")
       public Integer getPort() {
           demoService.toString();
           return demoService.getPort();
       }
   }
  ```



这只是简单的用法，它很强大，

基于 tianai-rpc的高扩展性以及架构的便捷性，基本可以实现你想实现的大部分需求

### 5. 其它使用方式详见 tianai-rpc



### 6. 如果你想了解它的一些其他扩展，可以了解下 

 1. 后处理器

    > # 注 ： 直接实现该接口然后注入到spring中，它会自动装配
    >
    > ```
    > RpcClientPostProcessor // client后处理器
    > RpcInvocationPostProcessor // provider 后处理器
    > ```

	2.  便捷工具类 `RpcContext` 可以直接使用该工具类在业务中做简单扩展

	3.  基于SPI的 `Registry`, `RemotingDataCodec`, `RemotingClient`,`RemotingServer`, `LoadBalance`

     这些接口都可以自定义扩展， 

     实现该接口后在`META-INF/tianai-rpc/` 目录下按照规范编写对应是SPI文件即可自动装配到 tianai-rpc中

     详情请参考 tianai-rpc框架 
     http://www.gitee.com/tianai/tianai-rpc
- qq群: 1021884609