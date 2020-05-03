package cloud.tianai.rpc.springboot;

import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.LinkedList;
import java.util.List;

@Data
public class RpcConsumerBean<T> implements FactoryBean<T>, BeanFactoryAware {

    private static List<RpcClientPostProcessor> rpcClientPostProcessors;

    private String id;
    /**
     * 该参数只针对于该rpc服务初始化指定的参数.
     */
    private int requestTimeout;

    /**
     * 支持 JDK_PROXY 和 JAVASSIST_PROXY.
     */
    private String proxy;

    private Class<?> injectedType;

    private T source;

    private ConfigurableListableBeanFactory beanFactory;

    public RpcConsumerBean() {
    }

    public RpcConsumerBean(int requestTimeout, String proxy, Class<?> injectedType, T source) {
        this.requestTimeout = requestTimeout;
        this.proxy = proxy;
        this.injectedType = injectedType;
        this.id = injectedType.getSimpleName();
        this.source = source;
    }
    public RpcConsumerBean(String id, int requestTimeout, String proxy, Class<?> injectedType, T source) {
        this.requestTimeout = requestTimeout;
        this.proxy = proxy;
        this.injectedType = injectedType;
        this.id = id;
        this.source = source;
    }

    @Override
    public T getObject() throws Exception {
        if (source == null) {
            init();
        }
        return source;
    }

    private void init() {
        RpcProperties rpcProperties = beanFactory.getBean(RpcProperties.class);
        List<RpcClientPostProcessor> rpcClientPostProcessors = getRpcClientPostProcessors();
        RpcConsumerBuilder rpcConsumerBuilder = new RpcConsumerBuilder(injectedType, this, rpcProperties, rpcClientPostProcessors);
        source = (T) rpcConsumerBuilder.build();
    }

    private List<RpcClientPostProcessor> getRpcClientPostProcessors() {
        if (rpcClientPostProcessors == null) {
            synchronized (RpcConsumerBean.class) {
                if (rpcClientPostProcessors == null) {
                    rpcClientPostProcessors = new LinkedList<>();
                    String[] names = beanFactory.getBeanNamesForType(RpcClientPostProcessor.class, true, false);
                    for (String name : names) {
                        RpcClientPostProcessor bean = beanFactory.getBean(name, RpcClientPostProcessor.class);
                        rpcClientPostProcessors.add(bean);
                    }
                    // 排序
                    AnnotationAwareOrderComparator.sort(rpcClientPostProcessors);
                }
            }
        }
        return rpcClientPostProcessors;
    }

    @Override
    public Class<?> getObjectType() {
        return injectedType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
}
