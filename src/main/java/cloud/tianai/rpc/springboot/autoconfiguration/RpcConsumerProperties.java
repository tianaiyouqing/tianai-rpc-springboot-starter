package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc.client")
public class RpcConsumerProperties {
    private String client = "netty";
}
