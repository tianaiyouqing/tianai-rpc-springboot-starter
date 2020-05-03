package cloud.tianai.rpc.springboot.annotation;

import cloud.tianai.rpc.springboot.DefaultRpcProviderHandler;
import cloud.tianai.rpc.springboot.RpcProviderBean;
import cloud.tianai.rpc.springboot.properties.RpcProperties;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:42
 * @Description: 注解版的 RpcProviderHandler 适配器
 */
public class AnnotationRpcProviderHandler extends DefaultRpcProviderHandler {

    private Map<Object, RpcProvider> rpcProviderMap = new ConcurrentHashMap<Object, RpcProvider>(16);

    public AnnotationRpcProviderHandler(RpcProperties rpcProperties, boolean autoInit) {
        super(rpcProperties, autoInit);
    }

    public void registerProvider(Object bean, RpcProvider ann) {
        rpcProviderMap.remove(bean, ann);
        rpcProviderMap.put(bean, ann);
        RpcProviderBean rpcProviderBean = new AnnotationRpcProviderBeanAdapter(bean, ann);
        super.registerProvider(bean, rpcProviderBean);
    }

    public RpcProvider getProviderAnnotation(Object bean) {
        return rpcProviderMap.get(bean);
    }

    public Collection<RpcProvider> listProviderAnnotations() {
        return rpcProviderMap.values();
    }
}
