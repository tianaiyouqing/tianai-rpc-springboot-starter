package cloud.tianai.rpc.springboot;


import cloud.tianai.rpc.springboot.properties.RpcProperties;

import java.util.Collection;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:00
 * @Description: RpcProvider执行器
 */
public interface RpcProviderHandler {

    RpcProperties getRpcProperties();

    void registerProvider(Object bean, RpcProviderBean providerBean);

    RpcProviderBean getProvider(Object bean);

    Collection<RpcProviderBean> listProviderBeans();

    Collection<Object> listProviderSources();

    void init();

    boolean isInit();
}
