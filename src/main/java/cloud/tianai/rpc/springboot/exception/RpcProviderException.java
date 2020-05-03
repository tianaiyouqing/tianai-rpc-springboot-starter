package cloud.tianai.rpc.springboot.exception;

import cloud.tianai.rpc.common.exception.RpcException;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:04
 * @Description: RpcProvider异常
 */
public class RpcProviderException extends RpcException {
    public RpcProviderException() {
    }

    public RpcProviderException(String message) {
        super(message);
    }

    public RpcProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcProviderException(Throwable cause) {
        super(cause);
    }

    public RpcProviderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
