package cloud.tianai.rpc.springboot.exception;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:05
 * @Description: RpcProvider注册异常
 */
public class RpcProviderRegisterException extends RpcProviderException{

    public RpcProviderRegisterException() {
    }

    public RpcProviderRegisterException(String message) {
        super(message);
    }

    public RpcProviderRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcProviderRegisterException(Throwable cause) {
        super(cause);
    }

    public RpcProviderRegisterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
