package cloud.tianai.rpc.springboot.exception;

import cloud.tianai.rpc.common.exception.RpcException;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 21:51
 * @Description: RpcConsumer 相关异常父级
 */
public class RpcConsumerException extends RpcException {

    public RpcConsumerException() {
    }

    public RpcConsumerException(String message) {
        super(message);
    }

    public RpcConsumerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcConsumerException(Throwable cause) {
        super(cause);
    }

    public RpcConsumerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
