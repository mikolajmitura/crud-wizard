package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.core.metamodels.JavaTypeMetaModel.createWithRawClass;
import static pl.jalokim.crudwizard.core.utils.ElementsUtils.nullableElements;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.JavaTypeMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MethodSignatureMetaModel;
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
        Type methodReturnType = methodContext.resolveReturnType();

        return MethodSignatureMetaModel.builder()
            .returnType(createJavaTypeMetaModel(instanceClass, methodReturnType))
            .methodArguments(resolveMethodArguments(instanceClass, methodContext))
            .build();
    }

    private List<MethodArgumentMetaModel> resolveMethodArguments(Class<?> instanceClass, MethodGenericsContext methodContext) {
        Method method = methodContext.currentMethod();

        List<MethodArgumentMetaModel> methodArgumentMetaModels = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < method.getParameterCount(); parameterIndex++) {
            Annotation[] argumentAnnotations = method.getParameterAnnotations()[parameterIndex];
            Type parameterType = methodContext.resolveParameterType(parameterIndex);
            methodArgumentMetaModels.add(MethodArgumentMetaModel.builder()
                .annotations(nullableElements(argumentAnnotations).asSet())
                .argumentType(createJavaTypeMetaModel(instanceClass, parameterType))
                .build());
        }
        return methodArgumentMetaModels;
    }

    private JavaTypeMetaModel createJavaTypeMetaModel(Class<?> instanceClass, Type type) {
        JavaTypeMetaModel javaTypeMetaModel;
        if (type instanceof Class) {
            javaTypeMetaModel = createWithRawClass((Class<?>) type);
        } else {
            var javaType = jsonObjectMapper.createJavaType(type, instanceClass);
            javaTypeMetaModel = JavaTypeMetaModel.createWithType(type, javaType);
        }
        return javaTypeMetaModel;
    }
}
