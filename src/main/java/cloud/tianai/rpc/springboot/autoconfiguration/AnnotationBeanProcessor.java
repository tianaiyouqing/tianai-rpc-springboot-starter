package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.core.client.proxy.RpcProxy;
import cloud.tianai.rpc.core.client.proxy.impl.JdkRpcProxy;
import cloud.tianai.rpc.core.constant.RpcClientConfigConstant;
import cloud.tianai.rpc.springboot.annotation.RpcConsumer;
import cloud.tianai.rpc.springboot.annotation.RpcProvider;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AnnotationBeanProcessor implements BeanPostProcessor, ApplicationContextAware, BeanFactoryAware, ApplicationListener<ApplicationStartedEvent> {

    private Properties prop;
    private RpcConsumerProperties rpcConsumerProperties;
    private RpcProperties rpcProperties;
    private AbstractApplicationContext applicationContext;
    public static final String BANNER =
            "  _   _                   _                        \n" +
                    " | | (_)                 (_)                       \n" +
                    " | |_ _  __ _ _ __   __ _ _        _ __ _ __   ___ \n" +
                    " | __| |/ _` | '_ \\ / _` | |______| '__| '_ \\ / __|\n" +
                    " | |_| | (_| | | | | (_| | |______| |  | |_) | (__ \n" +
                    "  \\__|_|\\__,_|_| |_|\\__,_|_|      |_|  | .__/ \\___|\n" +
                    "                                       | |         \n" +
                    "                                       |_|         ";
    private Map<Object, RpcProvider> rpcProviderMap = new ConcurrentHashMap<Object, RpcProvider>(16);
    private ConfigurableListableBeanFactory beanFactory;

    public AnnotationBeanProcessor(RpcConsumerProperties rpcConsumerProperties, RpcProperties rpcProperties) {
        this.rpcConsumerProperties = rpcConsumerProperties;
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
        Properties rpcConsumerProp = findRpcConsumerConfig(rpcConsumer);
        RpcProxy rpcProxy = new JdkRpcProxy();
        Object proxy = rpcProxy.createProxy(type, rpcConsumerProp, true, true);
        return proxy;
    }

    private Properties findRpcConsumerConfig(RpcConsumer rpcConsumer) {
        Properties resultProp;
        if (prop != null) {
            resultProp = prop;
        } else {
            resultProp = prop = findCommonProp();
        }
        int requestTimeout = rpcConsumer.requestTimeout();
        if(requestTimeout <= 0) {
            // 设置默认的请求超时时间，可以当做全局使用
            requestTimeout = rpcConsumerProperties.getDefaultRequestTimeout();
        }
        resultProp.setProperty(RpcClientConfigConstant.TIMEOUT, String.valueOf(requestTimeout));
        resultProp.setProperty(RpcClientConfigConstant.REQUEST_TIMEOUT, String.valueOf(requestTimeout));
        return resultProp;
    }

    private Properties findCommonProp() {
        Properties properties = new Properties();
        if (rpcConsumerProperties == null) {
            throw new RpcException("TIANAI-RPC 读取公共客户端消息失败， 未配置 [RpcConsumerProperties]");
        }
        properties.setProperty(RpcClientConfigConstant.CODEC, rpcProperties.getCodec());
        properties.setProperty(RpcClientConfigConstant.REGISTER, rpcProperties.getRegistry());
        properties.setProperty(RpcClientConfigConstant.REGISTRY_HOST, rpcProperties.getRegistryAddress());
        properties.setProperty(RpcClientConfigConstant.REGISTRY_PORT, String.valueOf(0));
        properties.setProperty(RpcClientConfigConstant.PROTOCOL, String.valueOf(rpcConsumerProperties.getClient()));
        properties.setProperty(RpcClientConfigConstant.WORKER_THREADS, String.valueOf(rpcProperties.getWorkerThreads()));
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
        ServerBootstrap serverBootstrap = null;
        try {
            serverBootstrap = applicationContext.getBean(ServerBootstrap.class);
        } catch (NoSuchBeanDefinitionException e) {
            serverBootstrap = null;
        }
        if (!rpcProviderMap.isEmpty()) {
            if (serverBootstrap == null) {
                throw new RpcException("TIANAI-RPC 注册server时未找到 [ServerBootstrap], 待注册的类:" + rpcProviderMap.keySet());
            }
            ServerBootstrap finalServerBootstrap = serverBootstrap;
            rpcProviderMap.forEach((bean, anno) -> {
                Class<?> interfaceClass = bean.getClass().getInterfaces()[0];
                finalServerBootstrap.register(interfaceClass, bean);
                log.info("TIANAI-RPC SERVER register[{}]", interfaceClass.getName());
            });
            // 注册完的话直接情况即可， 优化内存
            rpcProviderMap.clear();
        }
    }
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory)beanFactory;
    }
}
