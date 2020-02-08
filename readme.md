# 这是对**TIANAI-RPC** 框架的**springboot**脚手架工具，方便配置启动

## 使用方式

- 在Springboot项目中引入依赖

```xml
<dependency>
    <groupId>cloud.tianai.rpc</groupId>
    <artifactId>tianai-rpc-springboot-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

- server端
- 配置服务注册、序列化、断开等等

相关配置
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
    # server端是否启动， 如果使用rpcServer，必须设置为true， 默认是false
    enable: true
    # server端启动时的端口， 默认20881
    port: 20883
    # 超时，单位 ms
    timeout: 5000
```
使用: 
要提供RPC服务的类上加上 **@RpcProvider**注解即可
```java
    @Service
    @RpcProvider
    public class DemoServiceImpl implements DemoService {
        
    }
```

- client端

配置:
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

要使用RPC服务的接口的field中加上 **@RpcConsumer** 注解即可
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

一些其他的相关配置详见:

- RpcConsumerProperties.java
- RpcProperties.java
- RpcProviderProperties.java

