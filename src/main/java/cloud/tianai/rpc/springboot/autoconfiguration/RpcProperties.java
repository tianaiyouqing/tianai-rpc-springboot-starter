package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc")
public class RpcProperties {
    private String codec = "hessian2";
    private String registry = "zookeeper";
    private String registryAddress = "127.0.0.1:2181";
    private Integer workerThreads = RpcConfigConstant.DEFAULT_IO_THREADS;
}
