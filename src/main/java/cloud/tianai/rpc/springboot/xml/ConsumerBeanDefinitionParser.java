package cloud.tianai.rpc.springboot.xml;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.springboot.RpcConsumerBean;
import cloud.tianai.rpc.springboot.exception.RpcConsumerParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 21:46
 * @Description: 解析 ConsumerBean
 */
public class ConsumerBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(RpcConsumerBean.class);
        beanDefinition.setLazyInit(false);

        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        String id = element.getAttribute("id");
        String interfaceClassStr = element.getAttribute("interface");
        String requestTimeoutStr = element.getAttribute("requestTimeout");
        String proxy = element.getAttribute("proxy");
        Class<?> interfaceClass;
        if (StringUtils.isBlank(interfaceClassStr)) {
            throw new RpcConsumerParseException("[interface] 属性不能为空", element);
        }

        try {
            interfaceClass = ClassUtils.forName(interfaceClassStr);
            if (!interfaceClass.isInterface()) {
                // 如果不是接口，直接抛出异常
                throw new RpcConsumerParseException("[interface] 必须执行接口的全路径", element);
            }
        } catch (ClassNotFoundException e) {
            throw new RpcConsumerParseException("[interface] 必须执行接口的全路径".concat(e.getMessage()), element);
        }

        // id
        if (StringUtils.isBlank(id)) {
            id = interfaceClass.getSimpleName();
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
        }

        // 超时
        if (StringUtils.isNotBlank(requestTimeoutStr)) {
            int requestTimeout = Integer.valueOf(requestTimeoutStr);
            propertyValues.add("requestTimeout", requestTimeout);
        }
        // proxy类型
        if (StringUtils.isNotBlank(proxy)) {
            propertyValues.add("proxy", proxy);

        }
        // 接口类型
        propertyValues.add("injectedType", interfaceClass);
        // 注册rootBeanDefinition
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }
}
