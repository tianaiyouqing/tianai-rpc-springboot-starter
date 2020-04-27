package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.springboot.annotation.TianAiRpcAnnotationBean;
import cloud.tianai.rpc.springboot.properties.RpcConsumerProperties;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import cloud.tianai.rpc.springboot.properties.RpcProviderProperties;
import cloud.tianai.rpc.springboot.properties.RpcReqistryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @Author: 天爱有情
 * @Date: 2020/02/03 13:53
 * @Description: TIANAI-RPC 自动装配
 */
@Configuration
@EnableConfigurationProperties({RpcProperties.class})
public class RpcAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TianAiRpcAnnotationBean annotationBean(RpcProperties rpcProperties) {
        return new TianAiRpcAnnotationBean(rpcProperties);
    }
}
