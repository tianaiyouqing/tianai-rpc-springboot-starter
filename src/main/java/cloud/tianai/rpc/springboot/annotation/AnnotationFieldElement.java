package cloud.tianai.rpc.springboot.annotation;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;

public class AnnotationFieldElement extends InjectionMetadata.InjectedElement{
    private final boolean required;
    private volatile boolean cached = false;
    @Nullable
    private volatile Object cachedFieldValue;
    private ConfigurableListableBeanFactory beanFactory;

    public AnnotationFieldElement(Field field, boolean required) {
        super(field, null);
        this.required = required;
    }

    @Override
    protected void inject(Object target, String beanName, PropertyValues pvs) throws Throwable {
        Field field = (Field) this.member;


    }

    @Nullable
    private Object resolvedCachedArgument(@Nullable String beanName, @Nullable Object cachedArgument) {
        if (cachedArgument instanceof DependencyDescriptor) {
            DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
            Assert.state(this.beanFactory != null, "No BeanFactory available");
            return this.beanFactory.resolveDependency(descriptor, beanName, null, null);
        }
        else {
            return cachedArgument;
        }
    }
}
