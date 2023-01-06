package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel.createWithRawClass;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.TypeNameWrapper;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper;
import ru.vyarus.java.generics.resolver.GenericsResolver;
import ru.vyarus.java.generics.resolver.context.GenericsContext;
import ru.vyarus.java.generics.resolver.context.MethodGenericsContext;

@Component
@RequiredArgsConstructor
public class MethodSignatureMetaModelResolver {

    private final JsonObjectMapper jsonObjectMapper;

    public MethodSignatureMetaModel resolveMethodSignature(Method method, Class<?> instanceClass) {
        GenericsContext context = GenericsResolver.resolve(instanceClass);
        MethodGenericsContext methodContext = context.method(method);
        Type methodReturnType = new TypeNameWrapper(methodContext.resolveReturnType());
        Class<?> rawReturnClass = methodContext.resolveReturnClass();

        return MethodSignatureMetaModel.builder()
            .returnType(createJavaTypeMetaModel(instanceClass, rawReturnClass, methodReturnType))
            .methodArguments(resolveMethodArguments(instanceClass, methodContext))
            .build();
    }

    public ClassMetaModel getMethodReturnClassMetaModel(BeanAndMethodDto beanAndMethodDto) {
        MethodSignatureMetaModel methodSignatureMetaModel = getMethodSignatureMetaModel(beanAndMethodDto);
        return classMetaModelFromType(methodSignatureMetaModel.getReturnType());
    }

    public MethodSignatureMetaModel getMethodSignatureMetaModel(BeanAndMethodDto beanAndMethodDto) {
        Class<?> beanClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
        Method foundMethod = findMethodByName(beanClass, beanAndMethodDto.getMethodName());
        return resolveMethodSignature(foundMethod, beanClass);
    }

    private List<MethodArgumentMetaModel> resolveMethodArguments(Class<?> instanceClass, MethodGenericsContext methodContext) {
        Method method = methodContext.currentMethod();

        List<MethodArgumentMetaModel> methodArgumentMetaModels = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < method.getParameterCount(); parameterIndex++) {
            Annotation[] argumentAnnotations = method.getParameterAnnotations()[parameterIndex];
            Type parameterType = new TypeNameWrapper(methodContext.resolveParameterType(parameterIndex));

            GenericsContext genericsContext = methodContext.parameterType(parameterIndex);

            methodArgumentMetaModels.add(MethodArgumentMetaModel.builder()
                .annotations(elements(argumentAnnotations).asList())
                .parameter(method.getParameters()[parameterIndex])
                .argumentType(createJavaTypeMetaModel(instanceClass, genericsContext.currentClass(), parameterType))
                .build());
        }
        return methodArgumentMetaModels;
    }

    private JavaTypeMetaModel createJavaTypeMetaModel(Class<?> contextClass, Class<?> rawClassOfType, Type type) {
        JavaTypeMetaModel javaTypeMetaModel;
        if (type instanceof Class) {
            javaTypeMetaModel = createWithRawClass((Class<?>) type);
        } else if (unwrap(type) instanceof Class) {
            Type unwrappedType = unwrap(type);
            javaTypeMetaModel = createWithRawClass((Class<?>) unwrappedType);
        } else {
            var javaType = jsonObjectMapper.createJavaType(unwrap(type), contextClass);
            javaTypeMetaModel = JavaTypeMetaModel.createWithType(rawClassOfType, type, javaType);
        }
        return javaTypeMetaModel;
    }

    private static Type unwrap(Type type) {
        if (type instanceof TypeNameWrapper) {
            return ((TypeNameWrapper) type).getWrappedType();
        }
        return type;
    }
}
