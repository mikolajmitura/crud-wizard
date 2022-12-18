package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.NULL_REFERENCE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.SERVICE_EXPECTED_ARGS_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgumentConfig.getCommonExpectedArgsTypeAndOther;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractorResolver.findTypePredicateAndDataExtractor;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.ArgumentValueExtractMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.EndpointQueryAndUrlMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.GenericServiceArgumentMethodProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.MethodParameterAndAnnotation;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.MethodParameterInfo;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.ResolvedValueForAnnotation;
import pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractor;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GodClass"})
public class DelegatedServiceMethodInvoker {

    private static final Map<HttpMethod, HttpStatus> STATUS_BY_METHOD = Map.of(
        HttpMethod.POST, HttpStatus.CREATED,
        HttpMethod.GET, HttpStatus.OK,
        HttpMethod.DELETE, HttpStatus.NO_CONTENT,
        HttpMethod.PUT, HttpStatus.NO_CONTENT,
        HttpMethod.PATCH, HttpStatus.NO_CONTENT
    );

    /**
     * arguments which can be resolved:
     *
     * by type EndpointMetaModel from {@link GenericServiceArgument.endpointMetaModel}
     *
     * by type {@link GenericServiceArgument}
     *
     * by type HttpServletRequest from {@link GenericServiceArgument.request}
     *
     * by type HttpServletResponse from {@link GenericServiceArgument.response}
     *
     * by type JsonNode from {@link GenericServiceArgument.requestBody}
     *
     * by type TranslatedPayload from {@link GenericServiceArgument.requestBodyTranslated}
     *
     * by type ValidationSessionContext from {@link GenericServiceArgument.validationContext} for validation in service see {@link ValidationSessionContext} for
     * usage of api, you can add validation message for some object node.
     *
     * by @RequestHeader as whole map {@link GenericServiceArgument.headers}
     *
     * by @RequestHeader by name from {@link GenericServiceArgument.headers} only as simple values
     *
     * by @RequestParam as map from {@link GenericServiceArgument.httpQueryTranslated} from GenericServiceArgument
     *
     * by @RequestParam by name from {@link GenericServiceArgument.httpQueryTranslated} converted to real simple class
     *
     * by @RequestBody as JsonNode from {@link GenericServiceArgument.requestBody}, as TranslatedPayload from {@link
     * GenericServiceArgument.requestBodyTranslated}, as String as raw json, or real java bean, dto translated from {@link GenericServiceArgument.requestBody}
     *
     * by @PathVariable by name from {@link GenericServiceArgument.urlPathParams}
     *
     * example:
     * <pre>
     * &#64;Service
     * public class UserService {
     *
     *   public List&#60;String&#62; updateUser(JsonNode userAsJsonNode, &#64;PathVariable Long userId,
     *      &#64;RequestParam Map&#60;String, Object&#62; allQueryParams,
     *      Map&#60;String, String&#62; allHeaders){
     *          // your service code
     *   }
     *
     *   public List&#60;String&#62; updateUserByJsonNode(&#64;RequestBody UserDto userDto, &#64;PathVariable("user-id") Long userId,
     *      &#64;RequestParam queryArg1, &#64;RequestParam("someVarName") queryArg1,
     *      &#64;RequestHeader headerName, &#64;RequestHeader("x-cookie) cookie){
     *        // your service code
     *   }
     * }
     * </pre>
     */

    public ResponseEntity<Object> callMethod(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        ServiceMetaModel serviceMetaModel = endpointMetaModel.getServiceMetaModel();
        List<Object> methodArguments = collectMethodArguments(genericServiceArgument, serviceMetaModel);
        Method originalMethod = serviceMetaModel.getServiceBeanAndMethod().getOriginalMethod();
        Object result = invokeMethod(serviceMetaModel.getServiceInstance(), originalMethod, methodArguments.toArray());
        return resolveReturnObject(result, endpointMetaModel);
    }

    private List<Object> collectMethodArguments(GenericServiceArgument genericServiceArgument, ServiceMetaModel serviceMetaModel) {

        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        EndpointQueryAndUrlMetaModel endpointQueryAndUrlMetaModel = EndpointQueryAndUrlMetaModel.builder()
            .queryArgumentsModel(endpointMetaModel.getQueryArguments())
            .pathParamsModel(endpointMetaModel.getPathParams())
            .build();

        var methodMetaModel = serviceMetaModel.getServiceBeanAndMethod();
        var methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<Object> methodArguments = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < methodSignatureMetaModel.getMethodArguments().size(); parameterIndex++) {
            var methodArgumentMetaModel = methodSignatureMetaModel.getMethodArguments().get(parameterIndex);
            var argumentType = methodArgumentMetaModel.getArgumentType();
            MethodParameterInfo methodParameterInfo = new MethodParameterInfo(argumentType, parameterIndex,
                methodArgumentMetaModel.getParameter().getName());

            ClassMetaModel typeOfInputServiceOrMapper = endpointMetaModel.getPayloadMetamodel();
            ClassMetaModel classMetaModelFromMethodArg = classMetaModelFromType(argumentType);

            TypePredicateAndDataExtractor typePredicateAndDataExtractor = findTypePredicateAndDataExtractor(
                getCommonExpectedArgsTypeAndOther(SERVICE_EXPECTED_ARGS_TYPE),
                typeOfInputServiceOrMapper,
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
                        .genericMethodArgumentProvider(createDataProvider(genericServiceArgument))
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

            addNewMethodArgumentValue(genericServiceArgument, methodArguments, methodParameterInfo,
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

    private void addNewMethodArgumentValue(GenericServiceArgument genericServiceArgument, List<Object> methodArguments,
        MethodParameterInfo methodParameterInfo, MethodArgumentMetaModel methodArgumentMetaModel, Object argumentToAdd) {
        if (argumentToAdd == null) {
            var annotationsAsText = elements(methodArgumentMetaModel.getAnnotations())
                .asConcatText(System.lineSeparator());

            throw new TechnicalException(String.format("Cannot resolve argument with type: %s,%nwith annotations: [%s]%n%s",
                methodParameterInfo.getArgumentMetaModel(), annotationsAsText, atIndexInMethod(methodParameterInfo.getIndex(), genericServiceArgument)));
        } else {
            if (argumentToAdd == NULL_REFERENCE) {
                methodArguments.add(null);
            } else {
                methodArguments.add(argumentToAdd);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> resolveReturnObject(Object result, EndpointMetaModel endpointMetaModel) {
        var successHttpCodeOptional = Optional.ofNullable(endpointMetaModel.getResponseMetaModel())
            .map(EndpointResponseMetaModel::getSuccessHttpCode);
        if (result == null) {
            return successHttpCodeOptional
                .map(integer -> ResponseEntity.status(integer).build())
                .orElseGet(() -> ResponseEntity.noContent().build());
        } else if (result instanceof ResponseEntity) {
            return (ResponseEntity<Object>) result;
        }
        HttpStatus httpStatus = successHttpCodeOptional
            .map(HttpStatus::resolve)
            .orElseGet(() -> STATUS_BY_METHOD.get(endpointMetaModel.getHttpMethod()));

        return ResponseEntity.status(httpStatus)
            .body(result);
    }

    private String atIndexInMethod(int parameterIndex, GenericServiceArgument genericServiceArgument) {
        var method = genericServiceArgument.getEndpointMetaModel().getServiceMetaModel()
            .getServiceBeanAndMethod().getOriginalMethod();
        int realIndex = parameterIndex + 1;
        return String.format("at index: %s%nin class: %s%nwith method name: %s%nin method : %s",
            realIndex, method.getDeclaringClass().getCanonicalName(), method.getName(), method);
    }

    private GenericServiceArgumentMethodProvider createDataProvider(GenericServiceArgument genericServiceArgument) {
        return new GenericServiceArgumentMethodProvider(genericServiceArgument.getEndpointMetaModel(),
            genericServiceArgument.getRequest(),
            genericServiceArgument.getResponse(),
            genericServiceArgument.getRequestBodyTranslated(),
            genericServiceArgument.getRequestBody(),
            genericServiceArgument.getHeaders(),
            genericServiceArgument.getHttpQueryTranslated(),
            genericServiceArgument.getUrlPathParams(),
            genericServiceArgument,
            genericServiceArgument.getValidationContext());
    }
}
