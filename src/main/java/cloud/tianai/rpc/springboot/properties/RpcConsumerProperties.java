package cloud.tianai.rpc.springboot.properties;

import cloud.tianai.rpc.core.client.proxy.RpcProxyType;
import cloud.tianai.rpc.core.loadbalance.impl.RandomLoadBalance;
import lombok.Data;

@Data
public class RpcConsumerProperties {
    private String client = "netty";

    /** 设置默认的超时时间，可以当做全局使用. */
    private Integer defaultRequestTimeout = 5000;

    /** 重试次数. */
    private Integer retry = 3;

    /** 默认是权重随机模式. */
    private String loadbalance = RandomLoadBalance.NAME;

    /** 懒加载服务注册. */
    private boolean lazyLoadRegistry = true;

    /** 懒加载rpc客户端. */
    private boolean lazyStartRpcClient = true;

    /** 默认使用字节码代理. */
    private RpcProxyType defaultProxyType = RpcProxyType.JAVASSIST_PROXY;
}
