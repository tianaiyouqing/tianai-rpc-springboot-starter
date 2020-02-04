package cloud.tianai.rpc.springboot.autoconfiguration;

import cloud.tianai.rpc.common.CommonConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc")
public class RpcProperties {
    private Boolean banner = true;
    private String codec = "hessian2";
    private Integer workerThreads = CommonConstant.DEFAULT_IO_THREADS;
}
