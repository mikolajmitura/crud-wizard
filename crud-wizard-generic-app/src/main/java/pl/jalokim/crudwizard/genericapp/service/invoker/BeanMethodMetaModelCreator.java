package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.clearCglibClassName;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanMethodMetaModel;

@Component
@RequiredArgsConstructor
public class BeanMethodMetaModelCreator {

    private final MethodSignatureMetaModelResolver methodSignatureMetaModelResolver;

    public BeanMethodMetaModel createBeanMethodMetaModel(String methodName, String className) {
        Class<?> realClass = loadRealClass(className);
        Method method = findMethodByName(realClass, methodName);
        return createBeanMethodMetaModel(method, realClass);
    }

    public BeanMethodMetaModel createBeanMethodMetaModel(Method method, Class<?> instanceClass) {
        Class<?> realClass = loadRealClass(clearCglibClassName(instanceClass.getCanonicalName()));

        return BeanMethodMetaModel.builder()
            .originalMethod(method)
            .name(method.getName())
            .methodSignatureMetaModel(methodSignatureMetaModelResolver.resolveMethodSignature(method, realClass))
            .build();
    }
}
