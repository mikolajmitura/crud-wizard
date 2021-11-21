package pl.jalokim.crudwizard.genericapp.util;

import static org.springframework.util.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
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

    private final ApplicationContext applicationContext;

    public <T> T createInstanceOrGetBean(String className) {
        Class<?> realClass = ClassUtils.loadRealClass(className);
        return (T) tryLoadAsSpringBean(realClass)
            .orElseGet(() -> tryCreateInstance(realClass));
    }

    private <T> Optional<T> tryLoadAsSpringBean(Class<?> realClass) {
        try {
            return Optional.of((T) applicationContext.getBean(realClass));
        } catch (BeansException ex) {
            return Optional.empty();
        }
    }

    private <T> T tryCreateInstance(Class<?> realClass) {
        Constructor<?>[] constructors = realClass.getConstructors();
        String className = realClass.getCanonicalName();
        if (constructors.length != 1) {
            throw new IllegalArgumentException("Cannot create instance: " + className + " due to other number than one constructor");
        }
        Constructor<?> constructor = constructors[0];

        if (isEmpty(elements(constructor.getParameters()).asList())) {
            return (T) InvokableReflectionUtils.newInstance(realClass);
        }
        TypeMetadata typeMetadataOfClass = MetadataReflectionUtils.getTypeMetadataFromClass(realClass);
        ConstructorMetadata metaForConstructor = typeMetadataOfClass.getMetaForConstructor(constructor);

        try {
            List<Object> constructorArgs = (List<Object>) elements(metaForConstructor.getParameters())
                .map(parameter -> applicationContext.getBean(parameter.getTypeOfParameter().getRawType()))
                .asList();
            return (T) InvokableReflectionUtils.newInstance(realClass, constructorArgs);
        } catch (Exception ex) {
            throw new IllegalArgumentException("problem during create validator instance of : " + className, ex);
        }
    }
}
