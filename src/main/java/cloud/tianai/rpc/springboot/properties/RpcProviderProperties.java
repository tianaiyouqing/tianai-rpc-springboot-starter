package cloud.tianai.rpc.springboot.properties;

import lombok.Data;

@Data
public class RpcProviderProperties {
    private String server = "netty";
    private Integer bossThreads = 1;
    private Integer timeout = 5000;
    private Integer port = 20881;
}
