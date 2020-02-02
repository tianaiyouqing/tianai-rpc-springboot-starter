# 这是对**TIANAI-RPC** 框架的**springboot**脚手架工具，方便配置启动

## 使用方式

- server端
要提供RPC服务的类上加上 **@RpcProvider**注解即可
```java
    @Service
    @RpcProvider
    public class DemoServiceImpl implements DemoService {
        
    }
```

- client端

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

