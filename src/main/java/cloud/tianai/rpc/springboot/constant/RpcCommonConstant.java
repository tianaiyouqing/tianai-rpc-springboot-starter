package cloud.tianai.rpc.springboot.constant;

import cloud.tianai.rpc.common.URL;

public interface RpcCommonConstant {

    URL DEFAULT_REGISTRY_URL = new URL("zookeeper", "127.0.0.1", 2181);
}
