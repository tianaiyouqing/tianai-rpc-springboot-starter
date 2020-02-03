# 这是对**TIANAI-RPC** 框架的**springboot**脚手架工具，方便配置启动

## 使用方式

- 在Springboot项目中引入依赖

```xml
<dependency>
    <groupId>cloud.tianai.rpc</groupId>
    <artifactId>tianai-rpc-springboot-starter</artifactId>
    <version>1.0.2</version>
</dependency>
```

- server端
- 配置服务注册、序列化、断开等等

相关配置
```yaml
tianai-rpc:
  # 服务注册， 默认 zookeeper
  registry: zookeeper
  # 服务注册的地址， 默认 127.0.0.1:2181
  registry-address: 127.0.0.1:2181
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
  # 指定服务注册，默认zookeeper
  registry: zookeeper
  # 指定注册地址 默认 127.0.0.1:2181
  registry-address: 127.0.0.1:2181
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

