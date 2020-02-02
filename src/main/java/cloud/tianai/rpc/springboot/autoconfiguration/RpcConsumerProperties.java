package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.util.IPUtils;
import cloud.tianai.rpc.core.constant.RpcConfigConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc.client")
public class RpcConsumerProperties {
    private String codec = "hessian2";
    private String register = "zookeeper";
    private String registerHost = IPUtils.getHostIp();
    private Integer registerPort = 2181;
    private String client = "netty";
    private Integer workerThreads = RpcConfigConstant.DEFAULT_IO_THREADS;
    private Integer requestTimeout = 3000;
}
