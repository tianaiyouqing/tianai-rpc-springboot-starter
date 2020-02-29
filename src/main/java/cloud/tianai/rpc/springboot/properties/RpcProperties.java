package cloud.tianai.rpc.springboot.properties;

import cloud.tianai.rpc.common.constant.CommonConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "tianai-rpc")
public class RpcProperties {
    private Boolean banner = true;
    private String codec = "protostuff";
    private Integer workerThreads = CommonConstant.DEFAULT_IO_THREADS;
}
