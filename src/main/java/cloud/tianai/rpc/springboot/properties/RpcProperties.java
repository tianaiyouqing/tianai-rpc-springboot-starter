package cloud.tianai.rpc.springboot.properties;

import cloud.tianai.rpc.common.constant.CommonConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @Author: 天爱有情
 * @Date: 2020/04/27 22:48
 * @Description: RPC配置
 */
@Data
@ConfigurationProperties(prefix = "tianai-rpc")
public class RpcProperties {

    /** 是否开启 banner. */
    private Boolean banner = true;

    /** 编码解码器. */
    private String codec = "hessian2";

    /** 默认工作线程. */
    private Integer workerThreads = CommonConstant.DEFAULT_IO_THREADS;

    /** 客户端. */
    @NestedConfigurationProperty
    private RpcConsumerProperties client = new RpcConsumerProperties();

    /** server端. */
    @NestedConfigurationProperty
    private RpcProviderProperties server = new RpcProviderProperties();

    /** 服务注册. */
    @NestedConfigurationProperty
    private RpcReqistryProperties registry = new RpcReqistryProperties();
}
