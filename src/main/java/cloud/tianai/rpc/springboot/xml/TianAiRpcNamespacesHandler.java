package cloud.tianai.rpc.springboot.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class TianAiRpcNamespacesHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("consumer", new ConsumerBeanDefinitionParser());
        registerBeanDefinitionParser("provider", new ProviderBeanDefinitionParser());
    }
}
