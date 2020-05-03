package cloud.tianai.rpc.springboot.xml;

import cloud.tianai.rpc.common.util.ClassUtils;
import cloud.tianai.rpc.springboot.RpcProviderBean;
import cloud.tianai.rpc.springboot.exception.RpcProviderParseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 22:25
 * @Description: ProviderBean 解析
 */
public class ProviderBeanDefinitionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(RpcProviderBean.class);
        beanDefinition.setLazyInit(false);

        String interfaceClassStr = element.getAttribute("interface");
        String weightStr = element.getAttribute("weight");
        String ref = element.getAttribute("ref");
        String id = element.getAttribute("id");
        Class<?> interfaceClass;
        ManagedMap<String, String> parameters = null;
        if (StringUtils.isBlank(interfaceClassStr)) {
            throw new RpcProviderParseException("[interface] 属性不能为空", element);
        }
        if (StringUtils.isBlank(ref)) {
            throw new RpcProviderParseException("[ref] 属性不能为空", element);
        }

        try {
            interfaceClass = ClassUtils.forName(interfaceClassStr);
            if (!interfaceClass.isInterface()) {
                // 如果不是接口，直接抛出异常
                throw new RpcProviderParseException("[interface] 必须执行接口的全路径", element);
            }
        } catch (ClassNotFoundException e) {
            throw new RpcProviderParseException("[interface] 必须执行接口的全路径".concat(e.getMessage()), element);
        }
        if (StringUtils.isBlank(id)) {
            id = interfaceClass.getSimpleName();
            beanDefinition.getPropertyValues().addPropertyValue("id", id);
        }
        // 解析parameters
        parameters = parseParameters(element.getChildNodes(), beanDefinition);

        MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        propertyValues.addPropertyValue("parameters", parameters);
        propertyValues.addPropertyValue("ref", ref);

        if (StringUtils.isNotBlank(weightStr)) {
            int weight = Integer.parseInt(weightStr);
            propertyValues.addPropertyValue("weight", weight);
        }
        // 注册rootBeanDefinition
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }

    private static ManagedMap<String, String> parseParameters(NodeList nodeList, RootBeanDefinition beanDefinition) {
        if (nodeList == null) {
            return null;
        }
        ManagedMap<String, String> parameters = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (!(nodeList.item(i) instanceof Element)) {
                continue;
            }
            Element element = (Element) nodeList.item(i);
            if ("parameter".equals(element.getNodeName())
                    || "parameter".equals(element.getLocalName())) {
                if (parameters == null) {
                    parameters = new ManagedMap<>();
                }
                String key = element.getAttribute("key");
                String value = element.getAttribute("value");
                parameters.put(key, value);
            }
        }
        return parameters;
    }
}
