package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.clearCglibClassName;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;

@Component
@RequiredArgsConstructor
public class BeanMethodMetaModelCreator {

    private final MethodSignatureMetaModelResolver methodSignatureMetaModelResolver;

    public BeanAndMethodMetaModel createBeanMethodMetaModel(String methodName, String className, String beanName) {
        Class<?> realClass = loadRealClass(className);
        Method method = findMethodByName(realClass, methodName);
        return createBeanMethodMetaModel(method, realClass, beanName);
    }

    public BeanAndMethodMetaModel createBeanMethodMetaModel(Method method, Class<?> instanceClass, String beanName) {
        Class<?> realClass = loadRealClass(clearCglibClassName(instanceClass.getCanonicalName()));

        return BeanAndMethodMetaModel.builder()
            .className(instanceClass.getCanonicalName())
            .beanName(beanName)
            .originalMethod(method)
            .methodName(method.getName())
            .methodSignatureMetaModel(methodSignatureMetaModelResolver.resolveMethodSignature(method, realClass))
            .build();
    }
}
