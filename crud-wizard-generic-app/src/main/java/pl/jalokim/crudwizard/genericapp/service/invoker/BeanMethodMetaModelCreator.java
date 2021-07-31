package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.clearCglibClassName;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.utils.reflection.ReflectionOperationException;

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

    public static Method findMethodByName(Class<?> fromClass, String methodName) {
        var foundMethods = elements(fromClass.getMethods())
            .filter(method -> method.getName().equals(methodName))
            .asList();

        if (foundMethods.isEmpty()) {
            throw new ReflectionOperationException("Cannot find method with name: "
                + methodName + " in class " + fromClass.getCanonicalName());
        }

        if (foundMethods.size() > 1) {
            String foundMethodsAsText = elements(foundMethods).asConcatText(System.lineSeparator());

            throw new IllegalStateException("found more than one method with name "
                + methodName + " in class " + fromClass.getCanonicalName() + " found methods: " + foundMethodsAsText);
        }

        return elements(fromClass.getMethods()).getFirst();
    }
}
