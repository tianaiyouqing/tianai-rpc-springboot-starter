package cloud.tianai.rpc.springboot;

import cloud.tianai.rpc.common.util.CollectionUtils;
import cloud.tianai.rpc.core.bootstrap.ServerBootstrap;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * @Author: 天爱有情
 * @Date: 2020/05/01 17:20
 * @Description: ServerBootstrap 创建器
 */
public class ServerBootstrapBuilder {

    private static final RpcProperties DEFAULT_RPC_PROPERTIES = new RpcProperties();

    @Setter
    @Getter
    @Accessors(chain = true)
    private RpcProperties rpcProperties = DEFAULT_RPC_PROPERTIES;

    @Setter
    @Getter
    @Accessors(chain = true)
    private List<RpcInvocationPostProcessor> rpcInvocationPostProcessors;

    private ServerBootstrap serverBootstrap;

    public ServerBootstrap buildAndStart() {
        build().start();
        return serverBootstrap;
    }


    public ServerBootstrap build() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        this.serverBootstrap = serverBootstrap;
//        RpcServerConfiguration prop = serverBootstrap.getProp();
        serverBootstrap
                // 服务注册
                .registry(rpcProperties.getRegistry().getURL())
                // 编解码
                .codec(rpcProperties.getCodec())
                // 超时
                .timeout(rpcProperties.getServer().getTimeout())
                // 协议
                .protocol(rpcProperties.getServer().getServer())
                // 工作线程
                .workThreads(rpcProperties.getWorkerThreads())
                // 端口
                .port(rpcProperties.getServer().getPort())
                // boss线程
                .bossThreads(rpcProperties.getServer().getBossThreads());

        // 读取对应的invocationPostProcessor并进行装配
        List<RpcInvocationPostProcessor> rpcInvocationPostProcessors = getRpcInvocationPostProcessors();
        if (CollectionUtils.isNotEmpty(rpcInvocationPostProcessors)) {
            // 添加解析器
            rpcInvocationPostProcessors.forEach(serverBootstrap::addRpcInvocationPostProcessor);
        }
        return serverBootstrap;
    }

}
