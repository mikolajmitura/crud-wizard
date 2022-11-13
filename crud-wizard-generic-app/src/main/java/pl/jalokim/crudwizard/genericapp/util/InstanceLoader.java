package pl.jalokim.crudwizard.genericapp.util;

import static org.springframework.util.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.utils.reflection.ConstructorMetadata;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class InstanceLoader {

    private static final AtomicReference<InstanceLoader> INSTANCE_LOADER = new AtomicReference<>();

    private final ApplicationContext applicationContext;
    private final Map<Class<?>, Object> notSpringBeanInstancesByClass = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        INSTANCE_LOADER.set(this);
    }

    public static InstanceLoader getInstance() {
        return INSTANCE_LOADER.get();
    }

    public <T> T createInstanceOrGetBean(String className) {
        return createInstanceOrGetBean(className, null);
    }

    public <T> T createInstanceOrGetBean(String className, String beanName) {
        Class<?> realClass = ClassUtils.loadRealClass(className);
        return createInstanceOrGetBean(realClass, beanName);
    }

    public <T> T createInstanceOrGetBean(Class<?> realClass) {
        return createInstanceOrGetBean(realClass, null);
    }

    public <T> T createInstanceOrGetBean(Class<?> realClass, String beanName) {
        return (T) tryLoadAsSpringBean(realClass, beanName)
            .orElseGet(() -> tryGetOrCreateNotSpringInstance(realClass));
    }

    private <T> Optional<T> tryLoadAsSpringBean(Class<?> realClass, String nullableBeanName) {
        try {
            return Optional.ofNullable(nullableBeanName)
                .map(beanName -> (T) applicationContext.getBean(beanName, realClass))
                .or(() -> Optional.of((T) applicationContext.getBean(realClass)));
        } catch (BeansException ex) {
            return Optional.empty();
        }
    }

    private <T> T tryGetOrCreateNotSpringInstance(Class<?> realClass) {
        Object cachedInstance = notSpringBeanInstancesByClass.get(realClass);
        if (cachedInstance != null) {
            return (T) cachedInstance;
        }

        Constructor<?>[] constructors = realClass.getDeclaredConstructors();
        String className = realClass.getCanonicalName();
        if (constructors.length != 1) {
            throw new IllegalArgumentException("Cannot create instance: " + className + " due to other number than one constructor");
        }
        Constructor<?> constructor = constructors[0];

        if (isEmpty(elements(constructor.getParameters()).asList())) {
            Object createdInstance = InvokableReflectionUtils.newInstance(realClass);
            notSpringBeanInstancesByClass.put(realClass, createdInstance);
            return (T) InvokableReflectionUtils.newInstance(realClass);
        }
        TypeMetadata typeMetadataOfClass = MetadataReflectionUtils.getTypeMetadataFromClass(realClass);
        ConstructorMetadata metaForConstructor = typeMetadataOfClass.getMetaForConstructor(constructor);

        try {
            List<Object> constructorArgs = elements(metaForConstructor.getParameters())
                .map(parameter -> createInstanceOrGetBean(parameter.getTypeOfParameter().getRawType()))
                .asList();

            Object createdInstance = InvokableReflectionUtils.newInstance(realClass, constructorArgs);
            notSpringBeanInstancesByClass.put(realClass, createdInstance);

            return (T) createdInstance;
        } catch (Exception ex) {
            throw new IllegalArgumentException("problem during create instance of : " + className, ex);
        }
    }

    public void clearInstancesCache() {
        notSpringBeanInstancesByClass.clear();
    }
}
