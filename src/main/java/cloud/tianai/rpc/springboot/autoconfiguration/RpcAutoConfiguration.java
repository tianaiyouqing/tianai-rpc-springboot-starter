package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.URL;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({RpcConsumerProperties.class, RpcProperties.class, RpcProviderProperties.class})
public class RpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AnnotationBeanProcessor annotationBeanProcessor(RpcConsumerProperties rpcConsumerProperties) {
        return new AnnotationBeanProcessor(rpcConsumerProperties);
    }

    @Bean()
    @ConditionalOnProperty(value = "tianai-rpc.server.enable", havingValue = "true")
    public ServerBootstrap serverBootstrap(RpcProviderProperties rpcProviderProperties, RpcProperties rpcProperties, RpcConsumerProperties rpcConsumerProperties) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.codec(rpcProperties.getCodec())
                .timeout(rpcProviderProperties.getTimeout())
                .registry(new URL(rpcProperties.getRegistry(), rpcProperties.getRegistryHost(), rpcProperties.getRegistryPort()))
                .server(rpcProviderProperties.getServer())
                .port(rpcProviderProperties.getPort());
        serverBootstrap.start();
        return serverBootstrap;
    }
}
