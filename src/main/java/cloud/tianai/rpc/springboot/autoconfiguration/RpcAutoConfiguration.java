package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Author: 天爱有情
 * @Date: 2020/02/03 13:53
 * @Description: TIANAI-RPC 自带装配
 */
@Configuration
@EnableConfigurationProperties({RpcConsumerProperties.class, RpcProperties.class, RpcProviderProperties.class})
public class RpcAutoConfiguration implements InitializingBean {

    @Bean
    @ConditionalOnMissingBean
    public AnnotationBeanProcessor annotationBeanProcessor(RpcConsumerProperties rpcConsumerProperties, RpcProperties rpcProperties) {
        return new AnnotationBeanProcessor(rpcConsumerProperties, rpcProperties);
    }

    @Bean()
    @ConditionalOnProperty(value = "tianai-rpc.server.enable", havingValue = "true")
    public ServerBootstrap serverBootstrap(RpcProviderProperties rpcProviderProperties, RpcProperties rpcProperties, RpcConsumerProperties rpcConsumerProperties) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.codec(rpcProperties.getCodec())
                .timeout(rpcProviderProperties.getTimeout())
                .registry(new URL(rpcProperties.getRegistry(), rpcProperties.getRegistryAddress(), 0))
                .server(rpcProviderProperties.getServer())
                .port(rpcProviderProperties.getPort());
        serverBootstrap.start();
        return serverBootstrap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
