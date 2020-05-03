package cloud.tianai.rpc.springboot;

import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.springboot.exception.RpcProviderRegisterException;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static cloud.tianai.rpc.common.constant.CommonConstant.WEIGHT_KEY;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:42
 * @Description: 默认的RpcProviderHandler执行器
 */
@Slf4j
public class DefaultRpcProviderHandler implements RpcProviderHandler, BeanFactoryAware, ApplicationListener<ApplicationStartedEvent> {

    /**
     * 存储RpcProviderBean的容器.
     */
    private Map<Object, RpcProviderBean> rpcProviderMap = new ConcurrentHashMap<>(16);
    private RpcProperties rpcProperties;
    private ConfigurableListableBeanFactory beanFactory;
    private boolean autoInit;
    private AtomicBoolean init = new AtomicBoolean(false);

    public DefaultRpcProviderHandler(RpcProperties rpcProperties, boolean autoInit) {
        this.rpcProperties = rpcProperties;
        this.autoInit = autoInit;

    }

    @Override
    public RpcProperties getRpcProperties() {
        return rpcProperties;
    }

    @Override
    public void registerProvider(Object bean, RpcProviderBean providerBean) {
        if (isInit()) {
            throw new RpcProviderRegisterException("请在 执行 init() 方法前进行注册Provider");
        }
        rpcProviderMap.remove(bean);
        rpcProviderMap.put(bean, providerBean);
    }

    @Override
    public RpcProviderBean getProvider(Object bean) {
        return rpcProviderMap.get(bean);
    }

    @Override
    public Collection<RpcProviderBean> listProviderBeans() {
        return rpcProviderMap.values();
    }

    @Override
    public Collection<Object> listProviderSources() {
        return rpcProviderMap.keySet();
    }

    @Override
    public void init() {
        if (!init.compareAndSet(false, true)) {
            log.warn("[{}] 已经被初始化，不可重复调用", this.getClass().getName());
            return;
        }
        if (getRpcProviderMap().isEmpty()) {
            return;
        }
        ServerBootstrap serverBootstrap = getServerBootstrap();
        final ServerBootstrap finalServerBootstrap = serverBootstrap;
        getRpcProviderMap().forEach((bean, providerBean) -> {
            Class<?> targetClass;
            if (AopUtils.isAopProxy(bean)) {
                targetClass = AopUtils.getTargetClass(bean);
            } else {
                targetClass = bean.getClass();
            }
            Class<?> interfaceClass = targetClass.getInterfaces()[0];
            Map<String, Object> paramMap = new HashMap<>(8);
            Map<String, String> parameters = providerBean.getParameters();
            if (!CollectionUtils.isEmpty(parameters)) {
                parameters.forEach(paramMap::put);
            }
            // 权重
            paramMap.put(WEIGHT_KEY, String.valueOf(providerBean.getWeight()));
            // 注册
            finalServerBootstrap.register(interfaceClass, bean, paramMap);
            log.info("TIANAI-RPC SERVER register[{}]", interfaceClass.getName());
        });
        // 注册完的话直接清空即可， 优化内存
        this.rpcProviderMap.clear();
    }

    private ServerBootstrap getServerBootstrap() {
        ServerBootstrap serverBootstrap;
        try {
            serverBootstrap = beanFactory.getBean(ServerBootstrap.class);
        } catch (NoSuchBeanDefinitionException e) {
            synchronized (this) {
                try {
                    serverBootstrap = beanFactory.getBean(ServerBootstrap.class);
                } catch (NoSuchBeanDefinitionException ex) {
                    serverBootstrap = createServerBootstrap();
                    beanFactory.registerSingleton(serverBootstrap.getClass().getName(), serverBootstrap);
                }
            }
        }
        return serverBootstrap;
    }

    private ServerBootstrap createServerBootstrap() {
        // 读取对应的invocationPostProcessor并进行装配
        List<RpcInvocationPostProcessor> rpcInvocationPostProcessors = getRpcInvocationPostProcessors();
        ServerBootstrap serverBootstrap = new ServerBootstrapBuilder()
                .setRpcProperties(rpcProperties)
                .setRpcInvocationPostProcessors(rpcInvocationPostProcessors)
                .buildAndStart();
        return serverBootstrap;
    }

    private List<RpcInvocationPostProcessor> getRpcInvocationPostProcessors() {
        List<RpcInvocationPostProcessor> result = new LinkedList<>();
        String[] names = beanFactory.getBeanNamesForType(RpcInvocationPostProcessor.class, true, false);
        for (String name : names) {
            RpcInvocationPostProcessor bean = beanFactory.getBean(name, RpcInvocationPostProcessor.class);
            result.add(bean);
        }
        // 排序
        AnnotationAwareOrderComparator.sort(result);
        return result;
    }

    @Override
    public boolean isInit() {
        return init.get();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    public void setRpcProviderMap(Map<Object, RpcProviderBean> rpcProviderMap) {
        this.rpcProviderMap = rpcProviderMap;
    }

    public Map<Object, RpcProviderBean> getRpcProviderMap() {
        return rpcProviderMap;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        if (autoInit && !isInit()) {
            init();
        }
    }
}
