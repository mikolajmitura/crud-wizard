package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import static org.springframework.util.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;
import pl.jalokim.utils.reflection.ConstructorMetadata;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@Component
@RequiredArgsConstructor
public class ValidatorInstanceCache {

    private final Map<String, DataValidator<?>> dataValidatorsByKey = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    public DataValidator<?> loadInstance(String className) {
        return dataValidatorsByKey
            .computeIfAbsent(className, (realClassName) -> {
                Class<?> validatorClass = ClassUtils.loadRealClass(className);

                Constructor<?>[] constructors = validatorClass.getConstructors();
                if (constructors.length != 1) {
                    throw new IllegalArgumentException("Cannot create validator instance: " + className + " due to other number than one constructor");
                }
                Constructor<?> constructor = constructors[0];

                if (isEmpty(elements(constructor.getParameters()).asList())) {
                    return (DataValidator<?>) InvokableReflectionUtils.newInstance(validatorClass);
                }
                TypeMetadata typeMetadataValidator = MetadataReflectionUtils.getTypeMetadataFromClass(validatorClass);
                ConstructorMetadata metaForConstructor = typeMetadataValidator.getMetaForConstructor(constructor);

                try {
                    List<Object> constructorArgs = (List<Object>) elements(metaForConstructor.getParameters())
                        .map(parameter -> applicationContext.getBean(parameter.getTypeOfParameter().getRawType()))
                        .asList();
                    return (DataValidator<?>) InvokableReflectionUtils.newInstance(validatorClass, constructorArgs);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("problem during create validator instance of : " + className, ex);
                }
            });
    }
}
