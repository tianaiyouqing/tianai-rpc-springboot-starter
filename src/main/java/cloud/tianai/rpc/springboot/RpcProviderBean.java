package cloud.tianai.rpc.springboot;

import cloud.tianai.rpc.springboot.exception.RpcProviderException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:57
 * @Description: ProviderBean
 */
@Data
public class RpcProviderBean implements BeanFactoryAware, InitializingBean {
    /** 权重. */
    private int weight;

    /** ID. */
    private String id;

    /** 参数. */
    private Map<String, String> parameters;

    /** source. */
    private Object source;

    /** ref. */
    private String ref;

    private BeanFactory beanFactory;

    private RpcProviderHandler rpcProviderHandler;
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.rpcProviderHandler = beanFactory.getBean(RpcProviderHandler.class);
        if (source == null && StringUtils.isNotBlank(ref)) {
            source = beanFactory.getBean(ref);
        }
        if (source == null) {
            throw new RpcProviderException("初始化Provider错误， 需要制定source 或者 ref参数");
        }
        rpcProviderHandler.registerProvider(source, this);
    }
}

