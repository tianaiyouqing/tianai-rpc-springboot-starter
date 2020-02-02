package cloud.tianai.rpc.springboot.annotation;


import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcConsumer {
    int timeout() default 5000;

    int requestTimeout() default 3000;
}
