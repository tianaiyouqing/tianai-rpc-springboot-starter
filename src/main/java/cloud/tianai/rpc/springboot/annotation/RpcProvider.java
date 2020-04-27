package cloud.tianai.rpc.springboot.annotation;

import java.lang.annotation.*;

/**
 * @Author: 天爱有情
 * @Date: 2020/03/03 15:55
 * @Description: RPC提供者
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcProvider {

    /**
     * 权重，默认100.
     * @return
     */
    int weight() default 100;

    /**
     * 自定义参数键值对，如: {key1, value1, key2, value2}
     */
    String[] parameters() default {};
}
