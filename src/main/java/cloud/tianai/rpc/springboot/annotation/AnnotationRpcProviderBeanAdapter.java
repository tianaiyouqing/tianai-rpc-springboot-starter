package cloud.tianai.rpc.springboot.annotation;

import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.springboot.RpcProviderBean;

import java.util.Map;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:38
 * @Description: RpcProviderBean 适配器
 */
public class AnnotationRpcProviderBeanAdapter extends RpcProviderBean {
    private Object source;
    private RpcProvider ann;

    public AnnotationRpcProviderBeanAdapter(Object source, RpcProvider ann) {
        this.source = source;
        this.ann = ann;
    }

    @Override
    public int getWeight() {
        return ann.weight();
    }

    @Override
    public Map<String, String> getParameters() {
        return CollectionUtils.toStringMap(ann.parameters());
    }

    @Override
    public Object getSource() {
        return source;
    }
}
