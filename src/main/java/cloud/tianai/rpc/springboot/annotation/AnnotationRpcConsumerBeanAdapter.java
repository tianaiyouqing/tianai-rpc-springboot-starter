package cloud.tianai.rpc.springboot.annotation;

import cloud.tianai.rpc.springboot.RpcConsumerBean;
import lombok.Getter;

@Getter
public class AnnotationRpcConsumerBeanAdapter<T> extends RpcConsumerBean<T> {
    private RpcConsumer rpcConsumer;

    public AnnotationRpcConsumerBeanAdapter(Class<?> injectedType, T source, RpcConsumer rpcConsumer) {
        super(rpcConsumer.requestTimeout(), rpcConsumer.proxy(), injectedType, source);
        this.rpcConsumer = rpcConsumer;
    }
}
