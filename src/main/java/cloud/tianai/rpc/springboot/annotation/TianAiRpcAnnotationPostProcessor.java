package cloud.tianai.rpc.springboot.annotation;

import cloud.tianai.rpc.common.exception.RpcException;
import cloud.tianai.rpc.common.util.RpcVersion;
import cloud.tianai.rpc.core.configuration.RpcClientConfiguration;
import cloud.tianai.rpc.remoting.api.RpcClientPostProcessor;
import cloud.tianai.rpc.remoting.api.RpcInvocationPostProcessor;
import cloud.tianai.rpc.springboot.RpcConsumerBean;
import cloud.tianai.rpc.springboot.RpcConsumerBuilder;
import cloud.tianai.rpc.springboot.properties.RpcProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;

/**
 * @Author: 天爱有情
 * @Date: 2020/02/08 15:18
 * @Description: @RpcProvider 和 @RpcConsumer 的解析器
 */
@Slf4j
public class TianAiRpcAnnotationPostProcessor implements SmartInstantiationAwareBeanPostProcessor, BeanFactoryAware {
    private RpcProperties rpcProperties;
    private AnnotationRpcProviderHandler rpcProviderHandler;
    public static final String BANNER =
            "  _   _                   _                        \n" +
                    " | | (_)                 (_)                       \n" +
                    " | |_ _  __ _ _ __   __ _ _        _ __ _ __   ___ \n" +
                    " | __| |/ _` | '_ \\ / _` | |______| '__| '_ \\ / __|\n" +
                    " | |_| | (_| | | | | (_| | |______| |  | |_) | (__ \n" +
                    "  \\__|_|\\__,_|_| |_|\\__,_|_|      |_|  | .__/ \\___|\n" +
                    "                                       | |         \n" +
                    "                                       |_|   Version:".concat(RpcVersion.getVersion());
    private ConfigurableListableBeanFactory beanFactory;
    private List<RpcClientPostProcessor> rpcClientPostProcessors;
    private final Set<Class<? extends Annotation>> annotationTypes = new LinkedHashSet<>(4);

        private final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache =
            new ConcurrentHashMap<>(32);

    public TianAiRpcAnnotationPostProcessor(RpcProperties rpcProperties, AnnotationRpcProviderHandler rpcProviderHandler) {
        this.rpcProperties = rpcProperties;
        printBannerIfNecessary(rpcProperties.getBanner());
        this.rpcProviderHandler = rpcProviderHandler;
        annotationTypes.add(RpcConsumer.class);
    }

    /**
     * 打印一些骚东西
     *
     * @param banner
     */
    private void printBannerIfNecessary(Boolean banner) {
        if (!banner) {
            return;
        }
        System.out.println(BANNER);
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        // 注入 @RpcConsumer 元数据
        InjectionMetadata metadata = findInjectionMetadata(bean.getClass(), beanName);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @" +annotationTypes.iterator().next().getSimpleName()
                    + " dependencies is failed", ex);
        }
        return pvs;
    }

    public InjectionMetadata findInjectionMetadata(Class<?> beanClass, String beanName) {
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : beanClass.getName());
        return injectionMetadataCache.computeIfAbsent(cacheKey, k -> buildRpcConsumerMetadata(beanClass));
    }


    private InjectionMetadata buildRpcConsumerMetadata(final Class<?> beanClass) {
        if (!AnnotationUtils.isCandidateClass(beanClass, this.annotationTypes)) {
            return InjectionMetadata.EMPTY;
        }
        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = beanClass;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();
            // 读取field,
            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                MergedAnnotation<?> ann = findAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (log.isInfoEnabled()) {
                            log.info("Autowired annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    currElements.add(new AnnotationFieldElement(field, ann));
                }
            });

            // 读取方法
            ReflectionUtils.doWithLocalMethods(targetClass, method -> {
                Method bridgedMethod = findBridgedMethod(method);
                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                MergedAnnotation<?> ann = findAnnotation(method);
                if (ann != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        if (log.isInfoEnabled()) {
                            log.info("Autowired annotation is not supported on static methods: " + method);
                        }
                        return;
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                    currElements.add(new AnnotatedMethodElement(method, pd, ann));
                }
            });

            elements.addAll(currElements);
            targetClass = targetClass.getSuperclass();
        }while (targetClass != Object.class);
        return new InjectionMetadata(beanClass, elements);
    }

    private MergedAnnotation<?> findAnnotation(AccessibleObject ao) {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.annotationTypes) {
            MergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }

    private List<RpcClientPostProcessor> getRpcClientPostProcessors() {
        if (rpcClientPostProcessors != null) {
            return rpcClientPostProcessors;
        }
        List<RpcClientPostProcessor> result = new LinkedList<>();
        String[] names = beanFactory.getBeanNamesForType(RpcClientPostProcessor.class, true, false);
        for (String name : names) {
            RpcClientPostProcessor bean = beanFactory.getBean(name, RpcClientPostProcessor.class);
            result.add(bean);
        }
        // 排序
        AnnotationAwareOrderComparator.sort(result);
        rpcClientPostProcessors = result;
        return result;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcProvider annotation = AnnotationUtils.findAnnotation(bean.getClass(), RpcProvider.class);
        if (annotation == null) {
            // 不做处理
            return bean;
        }
        if (bean.getClass().isInterface()) {
            throw new RpcException("TIANAI-RPC 注册 [" + bean.getClass() + "], 失败， 该类是个接口，不是具体实现，无法注册");
        }
        if (bean.getClass().getInterfaces().length < 1) {
            throw new RpcException("TIANAI-RPC 注册 [" + bean.getClass() + "], 失败， 该类没有实现任何接口，无法注册");
        }
        rpcProviderHandler.registerProvider(bean, annotation);
        return bean;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    public RpcConsumerBean<?> getConsumerBean(AccessibleObject target,
                                              MergedAnnotation ann,
                                              Class<?> injectedType,
                                              Object source) {
        RpcConsumer annotation = null;
        if (RpcConsumer.class.isAssignableFrom(ann.getType())) {
            annotation = AnnotationUtils.findAnnotation(target, RpcConsumer.class);
        }
        if (annotation == null) {
            return new RpcConsumerBean<>();
        }
        return new AnnotationRpcConsumerBeanAdapter<>(injectedType,source, annotation);

    }
    public Object getRpcConsumerValue(Class<?> injectedType, RpcConsumerBean<?> consumerBean) {
        Object result;
        try {
            result = beanFactory.getBean(injectedType);
        } catch (NoSuchBeanDefinitionException e) {
            // 如果没有这个bean， 进行创建
            RpcConsumerBuilder rpcConsumerBuilder = new RpcConsumerBuilder(injectedType, consumerBean, rpcProperties, getRpcClientPostProcessors());
            result = rpcConsumerBuilder.build();
            beanFactory.registerSingleton(result.getClass().getSimpleName(), result);

        }
        return result;
    }

    /**
     * @Author: 天爱有情
     * @Date: 2020/04/28 17:53
     * @Description: field注入
     */
    private class AnnotationFieldElement extends InjectionMetadata.InjectedElement{
        private MergedAnnotation ann;
        public AnnotationFieldElement(Field field, MergedAnnotation<?> ann) {
            super(field, null);
            this.ann = ann;
        }
        @Override
        protected void inject(Object target, String beanName, PropertyValues pvs) throws Throwable {

            Field field = (Field) this.member;

            Class<?> injectedType = field.getType();

            RpcConsumerBean<?> consumerBean = getConsumerBean(field, ann, injectedType, target);

            Object value = getRpcConsumerValue(injectedType, consumerBean);

            ReflectionUtils.makeAccessible(field);

            field.set(target, value);
        }


    }

    /**
     * @Author: 天爱有情
     * @Date: 2020/04/28 17:53
     * @Description: 方法注入 比如 set方法
     */
    private class AnnotatedMethodElement extends InjectionMetadata.InjectedElement {
        private final Method method;
        private MergedAnnotation ann;
        protected AnnotatedMethodElement(Method method, PropertyDescriptor pd, MergedAnnotation ann) {
            super(method, pd);
            this.method = method;
            this.ann = ann;
        }

        @Override
        protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
            assert pd != null;
            Class<?> injectedType = pd.getPropertyType();

            RpcConsumerBean<?> consumerBean = getConsumerBean(method, ann, injectedType, bean);

            Object value = getRpcConsumerValue(injectedType, consumerBean);

            ReflectionUtils.makeAccessible(method);

            method.invoke(bean, value);
        }

    }
}
