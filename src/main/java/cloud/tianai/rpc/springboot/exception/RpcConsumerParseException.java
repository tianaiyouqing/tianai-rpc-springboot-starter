package cloud.tianai.rpc.springboot.exception;

import org.w3c.dom.Element;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 21:52
 * @Description: RpcConsumer 解析异常
 */
public class RpcConsumerParseException extends RpcConsumerException{

    public RpcConsumerParseException() {
    }

    public RpcConsumerParseException(String message, Element element) {
        super("解析Consumer失败， message:".concat(message).concat("element:").concat(element.toString()));
    }

    public RpcConsumerParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcConsumerParseException(Throwable cause) {
        super(cause);
    }

    public RpcConsumerParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
