package cloud.tianai.rpc.springboot.autoconfiguration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc.client")
public class RpcConsumerProperties {
    private String client = "netty";

    /** 设置默认的超时时间，可以当做全局使用. */
    private Integer defaultRequestTimeout = 5000;
}
