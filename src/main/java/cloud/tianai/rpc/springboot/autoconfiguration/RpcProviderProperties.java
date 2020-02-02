package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc.server")
public class RpcProviderProperties {
    private String server = "netty";
    private Integer workerThreads = RpcConfigConstant.DEFAULT_IO_THREADS;
    private Integer bossThreads = 1;
    private Integer timeout = 5000;
    private Integer port = 20881;
}
