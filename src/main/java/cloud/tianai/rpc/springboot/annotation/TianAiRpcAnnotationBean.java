package cloud.tianai.rpc.springboot.annotation;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.core.client.proxy.RpcProxyFactory;
import cloud.tianai.rpc.core.client.proxy.RpcProxyType;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.core.configuration.RpcServerConfiguration;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.springboot.properties.RpcConsumerProperties;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import cloud.tianai.rpc.springboot.properties.RpcProviderProperties;
import cloud.tianai.rpc.springboot.properties.RpcReqistryProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cloud.tianai.rpc.common.constant.CommonConstant.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 15:18
 * @Description: @RpcProvider 和 @RpcConsumer 的解析器
 */
@Slf4j
public class TianAiRpcAnnotationBean implements BeanPostProcessor, ApplicationContextAware, BeanFactoryAware, ApplicationListener<ApplicationStartedEvent> {

    private RpcClientConfiguration prop;
    private RpcProperties rpcProperties;
    private AbstractApplicationContext applicationContext;
    private Map<Object, RpcProvider> rpcProviderMap = new ConcurrentHashMap<Object, RpcProvider>(16);
    public static final String BANNER =
            "  _   _                   _                        \n" +
                    " | | (_)                 (_)                       \n" +
                    " | |_ _  __ _ _ __   __ _ _        _ __ _ __   ___ \n" +
                    " | __| |/ _` | '_ \\ / _` | |______| '__| '_ \\ / __|\n" +
                    " | |_| | (_| | | | | (_| | |______| |  | |_) | (__ \n" +
                    "  \\__|_|\\__,_|_| |_|\\__,_|_|      |_|  | .__/ \\___|\n" +
                    "                                       | |         \n" +
                    "                                       |_|         ";
    private ConfigurableListableBeanFactory beanFactory;

    public TianAiRpcAnnotationBean(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
        printBannerIfNecessary(rpcProperties.getBanner());
    }

    /**
     * 打印一些骚东西
     *
     * @param banner
     */
    private void printBannerIfNecessary(Boolean banner) {
        if (!banner) {
            return;
        }
        System.out.println(BANNER);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            RpcConsumer rpcConsumer = field.getAnnotation(RpcConsumer.class);
            if (rpcConsumer != null) {
                Object value = getValue(rpcConsumer, field);
                if (value != null) {
                    try {
                        field.set(bean, value);
                    } catch (IllegalAccessException e) {
                        log.error("Failed to init remote service RpcConsumer at filed " + field.getName() + " in class " + bean.getClass().getName() + ", cause: " + e.getMessage(), e);
                    }
                }
            }

        }
        return bean;
    }

    private Object getValue(RpcConsumer rpcConsumer, Field field) {
        Class<?> type = field.getType();
        Object bean = null;
        try {
            bean = beanFactory.getBean(type);
            return bean;
        } catch (NoSuchBeanDefinitionException e) {
            // 如果没有这个bean， 进行创建
            bean = createRpcConsumer(type, rpcConsumer);
            beanFactory.registerSingleton(bean.getClass().getSimpleName(), bean);
            log.info("TIANAI-RPC CLIENT create:" + type);
        }
        return bean;
    }

    private Object createRpcConsumer(Class<?> type, RpcConsumer rpcConsumer) {
        RpcClientConfiguration rpcConsumerProp = findRpcConsumerConfig(rpcConsumer);
        RpcProxyType rpcProxyType = getRpcProxyType(rpcConsumer, rpcProperties);
        Object proxy = RpcProxyFactory.create(type, rpcConsumerProp, rpcProxyType);
        return proxy;
    }

    private RpcProxyType getRpcProxyType(RpcConsumer rpcConsumer, RpcProperties prop ) {
        String proxy = rpcConsumer.proxy();
        RpcProxyType rpcProxyType;
        try {
            rpcProxyType = RpcProxyType.valueOf(proxy);
        } catch (IllegalArgumentException e) {
            // 找不到枚举
            rpcProxyType = null;
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

    private RpcClientConfiguration findRpcConsumerConfig(RpcConsumer rpcConsumer) {
        RpcClientConfiguration resultProp;
        if (prop != null) {
            resultProp = new RpcClientConfiguration();
        } else {
            resultProp = prop = findCommonProp();
        }
        int requestTimeout = rpcConsumer.requestTimeout();
        if (requestTimeout <= 0) {
            // 设置默认的请求超时时间，可以当做全局使用
            requestTimeout = rpcProperties.getClient().getDefaultRequestTimeout();
        }
        resultProp.setTimeout(requestTimeout);
        resultProp.setRequestTimeout(requestTimeout);
        resultProp.setLazyLoadRegistry(rpcProperties.getClient().isLazyLoadRegistry());
        resultProp.setLazyStartRpcClient(rpcProperties.getClient().isLazyStartRpcClient());
        // 装配 RpcClientPostProcessor
        List<RpcClientPostProcessor> rpcClientPostProcessors = getRpcClientPostProcessors();
        if (CollectionUtils.isNotEmpty(rpcClientPostProcessors)) {
            // 排序
            AnnotationAwareOrderComparator.sort(rpcClientPostProcessors);
            rpcClientPostProcessors.forEach(resultProp::addRpcClientPostProcessor);
        }

        return resultProp;
    }


    private List<RpcClientPostProcessor> getRpcClientPostProcessors() {
        List<RpcClientPostProcessor> result = new LinkedList<>();
        String[] names = beanFactory.getBeanNamesForType(RpcClientPostProcessor.class, true, false);
        for (String name : names) {
            RpcClientPostProcessor bean = beanFactory.getBean(name, RpcClientPostProcessor.class);
            result.add(bean);
        }
        return result;
    }

    private List<RpcInvocationPostProcessor> getRpcInvocationPostProcessors() {
        List<RpcInvocationPostProcessor> result = new LinkedList<>();
        String[] names = beanFactory.getBeanNamesForType(RpcInvocationPostProcessor.class, true, false);
        for (String name : names) {
            RpcInvocationPostProcessor bean = beanFactory.getBean(name, RpcInvocationPostProcessor.class);
            result.add(bean);
        }
        return result;
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


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcProvider annotation = AnnotationUtils.findAnnotation(bean.getClass(), RpcProvider.class);
        if (annotation == null) {
            // 不做处理
            return bean;
        }
        if (bean.getClass().isInterface()) {
            throw new RpcException("TIANAI-RPC 注册 [" + bean.getClass() + "], 失败， 该类是个接口，不是具体实现，无法注册");
        }
        if (bean.getClass().getInterfaces().length < 1) {
            throw new RpcException("TIANAI-RPC 注册 [" + bean.getClass() + "], 失败， 该类没有实现任何接口，无法注册");
        }
        rpcProviderMap.put(bean, annotation);
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (rpcProviderMap.isEmpty()) {
            return;
        }
        ServerBootstrap serverBootstrap;
        try {
            serverBootstrap = beanFactory.getBean(ServerBootstrap.class);
        } catch (NoSuchBeanDefinitionException e) {
            serverBootstrap = createServerBootstrap();
            beanFactory.registerSingleton(serverBootstrap.getClass().getName(), serverBootstrap);
        }
        final ServerBootstrap finalServerBootstrap = serverBootstrap;
        rpcProviderMap.forEach((bean, anno) -> {
            Class<?> targetClass;
            if(AopUtils.isAopProxy(bean)) {
                targetClass = AopUtils.getTargetClass(bean);
            }else {
                targetClass = bean.getClass();
            }
            Class<?> interfaceClass = targetClass.getInterfaces()[0];
            Map<String, Object> paramMap = new HashMap<>(8);
            CollectionUtils.toStringMap(anno.parameters()).forEach(paramMap :: put);
            // 权重
            paramMap.put(WEIGHT_KEY, String.valueOf(anno.weight()));
            // 注册
            finalServerBootstrap.register(interfaceClass, bean, paramMap);
            log.info("TIANAI-RPC SERVER register[{}]", interfaceClass.getName());
        });
        // 注册完的话直接情况即可， 优化内存
        rpcProviderMap.clear();
    }

    private ServerBootstrap createServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
//        RpcServerConfiguration prop = serverBootstrap.getProp();
        serverBootstrap
                // 服务注册
                .registry(rpcProperties.getRegistry().getURL())
                // 编解码
                .codec(rpcProperties.getCodec())
                // 超时
                .timeout(rpcProperties.getServer().getTimeout())
                // 协议
                .protocol(rpcProperties.getServer().getServer())
                // 工作线程
                .workThreads(rpcProperties.getWorkerThreads())
                // 端口
                .port(rpcProperties.getServer().getPort())
                // boss线程
                .bossThreads(rpcProperties.getServer().getBossThreads());

        // 读取对应的invocationPostProcessor并进行装配
        List<RpcInvocationPostProcessor> rpcInvocationPostProcessors = getRpcInvocationPostProcessors();
        if (CollectionUtils.isNotEmpty(rpcInvocationPostProcessors)) {
            // 排序
            AnnotationAwareOrderComparator.sort(rpcInvocationPostProcessors);
            // 添加解析器
            rpcInvocationPostProcessors.forEach(serverBootstrap::addRpcInvocationPostProcessor);
        }
        // 启动
        serverBootstrap.start();
        return serverBootstrap;
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
}
