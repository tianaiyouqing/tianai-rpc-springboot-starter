package cloud.tianai.rpc.springboot.exception;

import org.w3c.dom.Element;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 21:52
 * @Description: RpcProvider 解析异常
 */
public class RpcProviderParseException extends RpcConsumerException{

    public RpcProviderParseException() {
    }

    public RpcProviderParseException(String message, Element element) {
        super("解析Provider失败， message:".concat(message).concat("element:").concat(element.toString()));
    }

    public RpcProviderParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcProviderParseException(Throwable cause) {
        super(cause);
    }

    public RpcProviderParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
