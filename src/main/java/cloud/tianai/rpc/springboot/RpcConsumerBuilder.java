package cloud.tianai.rpc.springboot;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.client.proxy.RpcProxyFactory;
import cloud.tianai.rpc.core.client.proxy.RpcProxyType;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;
import java.util.function.BiFunction;

import static cloud.tianai.rpc.common.constant.CommonConstant.RPC_WORKER_THREADS_KEY;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 18:17
 * @Description: RpcConsumer 构建器
 */
@Slf4j
@Getter
public class RpcConsumerBuilder {

    /** Type类型. */
    private Class<?> type;
    /** ConsumerBean. */
    private RpcConsumerBean<?> rpcConsumerBean;
    /** Properties. */
    private RpcProperties rpcProperties;
    /** 处理器. */
    private List<RpcClientPostProcessor> rpcClientPostProcessors;

    public RpcConsumerBuilder(Class<?> type,
                              RpcConsumerBean<?> rpcConsumerBean,
                              RpcProperties rpcProperties,
                              List<RpcClientPostProcessor> rpcClientPostProcessors) {
        this.type = type;
        this.rpcConsumerBean = rpcConsumerBean;
        this.rpcProperties = rpcProperties;
        this.rpcClientPostProcessors = rpcClientPostProcessors;
    }

    public Object build() {
        return build((clazz, c) -> null);
    }

    public Object build(BiFunction<Class<?>, RpcConsumerBean, Object> cache) {
        Object source = cache.apply(type, rpcConsumerBean);
        if (source == null){
            source = createRpcConsumer();
            log.info("TIANAI-RPC CLIENT create: {}", type);
        }
        return source;
    }

    private Object createRpcConsumer() {
        RpcClientConfiguration rpcConsumerProp = findRpcConsumerConfig();
        RpcProxyType rpcProxyType = getRpcProxyType(rpcConsumerBean, rpcProperties);
        Object proxy = RpcProxyFactory.create(type, rpcConsumerProp, rpcProxyType);
        return proxy;
    }

    private RpcProxyType getRpcProxyType(RpcConsumerBean rpcConsumer, RpcProperties prop ) {
        String proxy = rpcConsumer.getProxy();
        RpcProxyType rpcProxyType = null;
        if (StringUtils.isNotBlank(proxy)) {
            try {
                rpcProxyType = RpcProxyType.valueOf(proxy);
            } catch (IllegalArgumentException e) {
                // 找不到枚举
                rpcProxyType = null;
            }
        }
        if (rpcProxyType != null) {
            return rpcProxyType;
        }

        // 寻找一下默认配置
        rpcProxyType = prop.getClient().getDefaultProxyType();
        if (rpcProxyType == null) {
            rpcProxyType = RpcProxyType.JAVASSIST_PROXY;
        }
        return rpcProxyType;
    }

    private RpcClientConfiguration findRpcConsumerConfig() {
        RpcClientConfiguration properties = new RpcClientConfiguration();
        if (rpcProperties.getClient() == null) {
            throw new RpcException("TIANAI-RPC 读取公共客户端消息失败， 未配置 [RpcConsumerProperties]");
        }
        properties.setCodec(rpcProperties.getCodec());
        properties.setRegistryUrl(rpcProperties.getRegistry().getURL());
        properties.setProtocol(rpcProperties.getClient().getClient());
        properties.addParameter(RPC_WORKER_THREADS_KEY, rpcProperties.getWorkerThreads());
        properties.setRetry(rpcProperties.getClient().getRetry());
        properties.setLoadBalance(rpcProperties.getClient().getLoadbalance());

        int requestTimeout = rpcConsumerBean.getRequestTimeout();
        if (requestTimeout <= 0) {
            // 设置默认的请求超时时间，可以当做全局使用
            requestTimeout = rpcProperties.getClient().getDefaultRequestTimeout();
        }
        properties.setTimeout(requestTimeout);
        properties.setRequestTimeout(requestTimeout);
        properties.setLazyLoadRegistry(rpcProperties.getClient().isLazyLoadRegistry());
        properties.setLazyStartRpcClient(rpcProperties.getClient().isLazyStartRpcClient());
        // 装配 RpcClientPostProcessor
        List<RpcClientPostProcessor> rpcClientPostProcessors = getRpcClientPostProcessors();
        if (CollectionUtils.isNotEmpty(rpcClientPostProcessors)) {

            rpcClientPostProcessors.forEach(properties::addRpcClientPostProcessor);
        }
        return properties;
    }

    private RpcClientConfiguration findCommonProp() {
        RpcClientConfiguration properties = new RpcClientConfiguration();
        if (rpcProperties.getClient() == null) {
            throw new RpcException("TIANAI-RPC 读取公共客户端消息失败， 未配置 [RpcConsumerProperties]");
        }
        properties.setCodec(rpcProperties.getCodec());
        properties.setRegistryUrl(rpcProperties.getRegistry().getURL());
        properties.setProtocol(rpcProperties.getClient().getClient());
        properties.addParameter(RPC_WORKER_THREADS_KEY, rpcProperties.getWorkerThreads());
        properties.setRetry(rpcProperties.getClient().getRetry());
        properties.setLoadBalance(rpcProperties.getClient().getLoadbalance());
        return properties;
    }
}
