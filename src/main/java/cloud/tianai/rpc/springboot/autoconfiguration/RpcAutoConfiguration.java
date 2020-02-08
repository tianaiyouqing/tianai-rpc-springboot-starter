package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.springboot.processor.AnnotationBeanProcessor;
import cloud.tianai.rpc.springboot.properties.RpcConsumerProperties;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import cloud.tianai.rpc.springboot.properties.RpcProviderProperties;
import cloud.tianai.rpc.springboot.properties.RpcReqistryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Author: 天爱有情
 * @Date: 2020/02/03 13:53
 * @Description: TIANAI-RPC 自动装配
 */
@Configuration
@EnableConfigurationProperties({RpcConsumerProperties.class, RpcProperties.class, RpcReqistryProperties.class, RpcProviderProperties.class})
public class RpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AnnotationBeanProcessor annotationBeanProcessor(RpcConsumerProperties rpcConsumerProperties, RpcProperties rpcProperties, RpcReqistryProperties rpcReqistryProperties) {
        return new AnnotationBeanProcessor(rpcConsumerProperties, rpcReqistryProperties, rpcProperties);
    }

    @Bean()
    @ConditionalOnProperty(value = "tianai-rpc.server.enable", havingValue = "true")
    public ServerBootstrap serverBootstrap(RpcProviderProperties rpcProviderProperties, RpcProperties rpcProperties, RpcReqistryProperties rpcReqistryProperties) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.codec(rpcProperties.getCodec())
                .timeout(rpcProviderProperties.getTimeout())
                .registry(rpcReqistryProperties.getURL())
                .server(rpcProviderProperties.getServer())
                .port(rpcProviderProperties.getPort());
        serverBootstrap.start();
        return serverBootstrap;
    }
}
