package cloud.tianai.rpc.springboot.properties;

import cloud.tianai.rpc.core.loadbalance.impl.RandomLoadBalance;
import cloud.tianai.rpc.core.loadbalance.impl.RoundRobinLoadBalance;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc.client")
public class RpcConsumerProperties {
    private String client = "netty";

    /** 设置默认的超时时间，可以当做全局使用. */
    private Integer defaultRequestTimeout = 5000;

    /** 重试次数. */
    private Integer retry = 3;

    /** 默认是轮询模式. */
    private String loadbalance = RoundRobinLoadBalance.NAME;
}
