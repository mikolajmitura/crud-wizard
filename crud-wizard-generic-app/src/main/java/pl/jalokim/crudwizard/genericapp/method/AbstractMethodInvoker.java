package pl.jalokim.crudwizard.genericapp.method;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.NULL_REFERENCE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.getCommonExpectedArgsTypeAndOther;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractorResolver.findTypePredicateAndDataExtractor;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.ArgumentValueExtractMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.EndpointQueryAndUrlMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.MethodParameterAndAnnotation;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.MethodParameterInfo;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.ResolvedValueForAnnotation;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractor;

public abstract class AbstractMethodInvoker<A, R> {

    public R callMethod(A methodArgument) {
        BeanAndMethodMetaModel beanAndMethodMetaModel = getBeanAndMethodMetaModel(methodArgument);
        List<Object> methodArguments = collectMethodArguments(methodArgument, beanAndMethodMetaModel);
        Method originalMethod = beanAndMethodMetaModel.getOriginalMethod();
        Object instanceForInvoke = getInstanceForInvoke(methodArgument);
        return invokeMethod(instanceForInvoke, originalMethod, methodArguments.toArray());
    }

    protected abstract BeanAndMethodMetaModel getBeanAndMethodMetaModel(A methodArgument);

    protected abstract Object getInstanceForInvoke(A methodArgument);

    protected abstract GenericMethodArgumentProvider createDataProvider(A methodArgument);

    protected abstract EndpointQueryAndUrlMetaModel getEndpointQueryAndUrlMetaModel(A methodArgument);

    protected abstract ClassMetaModel getTypeOfInputDueToMetaModel(A methodArgument);

    protected abstract List<GenericMethodArgument> getGenericMethodAdditionalConfig();

    private List<Object> collectMethodArguments(A methodArgument, BeanAndMethodMetaModel methodMetaModel) {
        ClassMetaModel typeOfInputDueToMetaModel = getTypeOfInputDueToMetaModel(methodArgument);
        EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel = getEndpointQueryAndUrlMetaModel(methodArgument);

        var methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<Object> methodArguments = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < methodSignatureMetaModel.getMethodArguments().size(); parameterIndex++) {
            var methodArgumentMetaModel = methodSignatureMetaModel.getMethodArguments().get(parameterIndex);
            var argumentType = methodArgumentMetaModel.getArgumentType();
            MethodParameterInfo methodParameterInfo = new MethodParameterInfo(argumentType, parameterIndex,
                methodArgumentMetaModel.getParameter().getName());

            ClassMetaModel classMetaModelFromMethodArg = classMetaModelFromType(argumentType);

            TypePredicateAndDataExtractor typePredicateAndDataExtractor = findTypePredicateAndDataExtractor(
                getCommonExpectedArgsTypeAndOther(getGenericMethodAdditionalConfig()),
                typeOfInputDueToMetaModel,
                methodArgumentMetaModel,
                classMetaModelFromMethodArg,
                endpointQueryAndUrlMetaModel);

            Object argumentToAdd = null;
            if (typePredicateAndDataExtractor != null) {
                MethodParameterAndAnnotation methodParameterAndAnnotation = createMethodParameterAndAnnotation(
                    typePredicateAndDataExtractor, methodParameterInfo,
                    methodMetaModel.getOriginalMethod(), methodArgumentMetaModel.getParameter());
                argumentToAdd = typePredicateAndDataExtractor.getExtractDataFunction()
                    .apply(ArgumentValueExtractMetaModel.builder()
                        .genericMethodArgumentProvider(createDataProvider(methodArgument))
                        .methodParameterAndAnnotation(methodParameterAndAnnotation)
                        .build());

                GenericMethodArgument genericMethodArgument = typePredicateAndDataExtractor.getGenericMethodArgument();
                Function<ResolvedValueForAnnotation, Object> resolvedValueValidator = genericMethodArgument.getResolvedValueValidator();
                if (resolvedValueValidator != null) {
                    argumentToAdd = resolvedValueValidator.apply(new ResolvedValueForAnnotation(
                        methodParameterAndAnnotation.getAnnotation(),
                        argumentToAdd,
                        methodParameterAndAnnotation
                    ));
                }
            }

            addNewMethodArgumentValue(methodMetaModel.getOriginalMethod(), methodArguments, methodParameterInfo,
                methodArgumentMetaModel, argumentToAdd);
        }
        return methodArguments;
    }

    private MethodParameterAndAnnotation createMethodParameterAndAnnotation(TypePredicateAndDataExtractor typePredicateAndDataExtractor,
        MethodParameterInfo methodParameterInfo, Method method, Parameter parameter) {

        GenericMethodArgument genericMethodArgument = typePredicateAndDataExtractor.getGenericMethodArgument();
        Class<? extends Annotation> annotatedWith = genericMethodArgument.getAnnotatedWith();
        Annotation annotation = null;
        if (annotatedWith != null) {
            annotation = parameter.getAnnotation(annotatedWith);
        }

        return new MethodParameterAndAnnotation(annotation, methodParameterInfo, method);
    }

    private void addNewMethodArgumentValue(Method method, List<Object> methodArguments,
        MethodParameterInfo methodParameterInfo, MethodArgumentMetaModel methodArgumentMetaModel, Object argumentToAdd) {
        if (argumentToAdd == null) {
            var annotationsAsText = elements(methodArgumentMetaModel.getAnnotations())
                .asConcatText(System.lineSeparator());

            throw new TechnicalException(String.format("Cannot resolve argument with type: %s,%nwith annotations: [%s]%n%s",
                methodParameterInfo.getArgumentMetaModel(), annotationsAsText, atIndexInMethod(methodParameterInfo.getIndex(), method)));
        } else {
            if (argumentToAdd == NULL_REFERENCE) {
                methodArguments.add(null);
            } else {
                methodArguments.add(argumentToAdd);
            }
        }
    }

    private String atIndexInMethod(int parameterIndex, Method method) {
        int realIndex = parameterIndex + 1;
        return String.format("at index: %s%nin class: %s%nwith method name: %s%nin method : %s",
            realIndex, method.getDeclaringClass().getCanonicalName(), method.getName(), method);
    }
}
