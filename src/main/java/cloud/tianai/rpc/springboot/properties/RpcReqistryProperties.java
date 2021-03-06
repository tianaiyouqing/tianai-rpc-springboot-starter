package cloud.tianai.rpc.springboot.properties;

import cloud.tianai.rpc.common.URL;
import lombok.Data;

import java.util.Map;

@Data
public class RpcReqistryProperties {
    private String name = "zookeeper";
    private String address = "127.0.0.1";
    private Map<String, String> otterProp;


    public URL getURL() {
        return new URL(this.getName(), this.getAddress(), 0, this.getOtterProp());
    }

}
