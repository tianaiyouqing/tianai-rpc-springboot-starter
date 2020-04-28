package cloud.tianai.rpc.springboot.annotation;

import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcConsumer {

    /** 该参数只针对于该rpc服务初始化指定的参数. */
    int requestTimeout() default 0;

    /** 支持 JDK_PROXY 和 JAVASSIST_PROXY. */
    String proxy() default "";
}
