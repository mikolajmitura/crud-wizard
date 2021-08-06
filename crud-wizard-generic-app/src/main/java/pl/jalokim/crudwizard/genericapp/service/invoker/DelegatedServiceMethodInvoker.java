package pl.jalokim.crudwizard.genericapp.service.invoker;

import static pl.jalokim.utils.collection.Elements.elements;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.JavaTypeMetaModel;
import pl.jalokim.crudwizard.core.metamodels.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel;
import pl.jalokim.crudwizard.core.utils.ReflectionUtils;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.utils.string.StringUtils;

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

    private static final List<Class<?>> REST_ANNOTATIONS = List.of(RequestHeader.class, RequestParam.class, RequestBody.class, PathVariable.class);

    private static final Object NULL_REFERENCE = new Object();

    private final JsonObjectMapper jsonObjectMapper;

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
     * by type JsonNode from urlPathParams from {@link GenericServiceArgument.requestBody}
     *
     * by type TranslatedPayload from {@link GenericServiceArgument.requestBodyTranslated}
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
        Method originalMethod = serviceMetaModel.getMethodMetaModel().getOriginalMethod();
        Object result = ReflectionUtils.invokeMethod(serviceMetaModel.getServiceInstance(), originalMethod, methodArguments.toArray());
        return resolveReturnObject(result, endpointMetaModel);
    }

    private List<Object> collectMethodArguments(GenericServiceArgument genericServiceArgument, ServiceMetaModel serviceMetaModel) {
        var methodMetaModel = serviceMetaModel.getMethodMetaModel();
        var methodSignatureMetaModel = methodMetaModel.getMethodSignatureMetaModel();
        List<Object> methodArguments = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < methodSignatureMetaModel.getMethodArguments().size(); parameterIndex++) {
            var methodArgumentMetaModel = methodSignatureMetaModel.getMethodArguments().get(parameterIndex);
            var argumentType = methodArgumentMetaModel.getArgumentType();
            Class<?> rawClassOfArgument = argumentType.getRawClass();
            Object argumentToAdd;
            MethodParameterInfo methodParameterInfo = new MethodParameterInfo(argumentType, parameterIndex,
                methodArgumentMetaModel.getParameter().getName());

            if (EndpointMetaModel.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument.getEndpointMetaModel();
            } else if (GenericServiceArgument.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument;
            } else if (HttpServletRequest.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument.getRequest();
            } else if (HttpServletResponse.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument.getResponse();
            } else if (JsonNode.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument.getRequestBody();
            } else if (TranslatedPayload.class.isAssignableFrom(rawClassOfArgument)) {
                argumentToAdd = genericServiceArgument.getRequestBodyTranslated();
            } else {
                Annotation firstRestAnnotation = getFirstRestAnnotation(methodArgumentMetaModel);
                argumentToAdd = resolveArgumentObjectByAnnotation(genericServiceArgument, firstRestAnnotation, methodParameterInfo);
            }

            addNewMethodArgumentValue(genericServiceArgument, methodArguments, methodParameterInfo,
                methodArgumentMetaModel, argumentToAdd);
        }
        return methodArguments;
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

    private static Annotation getFirstRestAnnotation(MethodArgumentMetaModel methodArgumentMetaModel) {
        for (Annotation annotation : methodArgumentMetaModel.getAnnotations()) {
            if (REST_ANNOTATIONS.contains(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    private Object resolveArgumentObjectByAnnotation(GenericServiceArgument genericServiceArgument, Annotation annotation,
        MethodParameterInfo methodParameterInfo) {
        if (annotation == null) {
            return null;
        }
        AtomicReference<Object> returnObjectRef = new AtomicReference<>();
        if (RequestHeader.class.isAssignableFrom(annotation.annotationType())) {
            resolveValueForHeaders(genericServiceArgument, (RequestHeader) annotation, returnObjectRef, methodParameterInfo);
        } else if (RequestParam.class.isAssignableFrom(annotation.annotationType())) {
            resolveValueForQueryParams(genericServiceArgument, (RequestParam) annotation, returnObjectRef, methodParameterInfo);
        } else if (RequestBody.class.isAssignableFrom(annotation.annotationType())) {
            resolveValueForRequestBody(genericServiceArgument, (RequestBody) annotation, returnObjectRef, methodParameterInfo);
        } else if (PathVariable.class.isAssignableFrom(annotation.annotationType())) {
            resolveValuePathVariable(genericServiceArgument, (PathVariable) annotation, returnObjectRef, methodParameterInfo);
        }
        return returnObjectRef.get();
    }

    private void resolveValueForHeaders(GenericServiceArgument genericServiceArgument, RequestHeader requestHeader,
        AtomicReference<Object> returnObjectRef, MethodParameterInfo methodParameterInfo) {
        var argumentMetaModel = methodParameterInfo.getArgumentMetaModel();
        getFirstValue(requestHeader.name(), requestHeader.value())
            .ifPresentOrElse(headerName ->
                    resolveHeaderValueByHeaderName(genericServiceArgument, requestHeader, returnObjectRef, argumentMetaModel, headerName),
                () -> {
                    if (Map.class.isAssignableFrom(argumentMetaModel.getRawClass())) {
                        Map<String, String> headers = genericServiceArgument.getHeaders();
                        if (headers == null && requestHeader.required()) {
                            informAboutRequiredAnnotation(requestHeader, methodParameterInfo.getIndex(), genericServiceArgument);
                        }
                        returnObjectRef.set(headers);
                    } else {
                        resolveHeaderValueByHeaderName(genericServiceArgument, requestHeader,
                            returnObjectRef, argumentMetaModel, methodParameterInfo.getName());
                    }
                });
    }

    private void resolveHeaderValueByHeaderName(GenericServiceArgument genericServiceArgument, RequestHeader requestHeader,
        AtomicReference<Object> returnObjectRef,
        JavaTypeMetaModel argumentMetaModel, String headerName) {
        String headerValue = Optional.ofNullable(genericServiceArgument.getHeaders())
            .map(headers -> headers.get(headerName))
            .orElse(null);

        if (headerValue == null) {
            if (requestHeader.required()) {
                throw new TechnicalException("Cannot find required header value with header name: " + headerName);
            }
            returnObjectRef.set(NULL_REFERENCE);
        } else {
            if (argumentMetaModel.getRawClass().isAssignableFrom(headerValue.getClass())) {
                returnObjectRef.set(headerValue);
            } else {
                returnObjectRef.set(jsonObjectMapper.convertToObject(headerValue, argumentMetaModel));
            }
        }
    }

    private void resolveValueForQueryParams(GenericServiceArgument genericServiceArgument, RequestParam requestParam,
        AtomicReference<Object> returnObjectRef, MethodParameterInfo methodParameterInfo) {
        var argumentMetaModel = methodParameterInfo.getArgumentMetaModel();
        getFirstValue(requestParam.name(), requestParam.value())
            .ifPresentOrElse(requestParamName ->
                    resolveQueryParamTypeByName(genericServiceArgument, requestParam, returnObjectRef, argumentMetaModel, requestParamName),
                () -> {
                    if (Map.class.isAssignableFrom(argumentMetaModel.getRawClass())) {
                        Map<String, Object> httpQueryTranslated = genericServiceArgument.getHttpQueryTranslated();
                        if (httpQueryTranslated == null && requestParam.required()) {
                            informAboutRequiredAnnotation(requestParam, methodParameterInfo.getIndex(), genericServiceArgument);
                        }
                        returnObjectRef.set(httpQueryTranslated);
                    } else {
                        resolveQueryParamTypeByName(genericServiceArgument, requestParam, returnObjectRef, argumentMetaModel, methodParameterInfo.getName());
                    }
                });
    }

    private void resolveQueryParamTypeByName(GenericServiceArgument genericServiceArgument, RequestParam requestParam, AtomicReference<Object> returnObjectRef,
        JavaTypeMetaModel argumentMetaModel, String requestParamName) {
        Object requestParamValue = genericServiceArgument.getHttpQueryTranslated().get(requestParamName);
        if (requestParamValue == null) {
            if (requestParam.required()) {
                throw new TechnicalException("Cannot find required http request parameter with name: " + requestParamName);
            }
            returnObjectRef.set(NULL_REFERENCE);
        } else {
            if (argumentMetaModel.getRawClass().isAssignableFrom(requestParamValue.getClass())) {
                returnObjectRef.set(requestParamValue);
            } else {
                returnObjectRef.set(jsonObjectMapper.convertToObject(requestParamValue.toString(), argumentMetaModel));
            }
        }
    }

    private void resolveValueForRequestBody(GenericServiceArgument genericServiceArgument, RequestBody requestBody,
        AtomicReference<Object> returnObjectRef, MethodParameterInfo methodParameterInfo) {
        if (genericServiceArgument.getRequestBody() == null) {
            if (requestBody.required()) {
                informAboutRequiredAnnotation(requestBody, methodParameterInfo.getIndex(), genericServiceArgument);
            } else {
                returnObjectRef.set(NULL_REFERENCE);
            }
        } else {
            Class<?> parameterClass = methodParameterInfo.getArgumentMetaModel().getRawClass();
            if (JsonNode.class.isAssignableFrom(parameterClass)) {
                returnObjectRef.set(genericServiceArgument.getRequestBody());
            } else if (TranslatedPayload.class.isAssignableFrom(parameterClass)) {
                returnObjectRef.set(genericServiceArgument.getRequestBodyTranslated());
            } else {
                returnObjectRef.set(jsonObjectMapper.convertToObject(ObjectNodePath.rootNode(),
                    genericServiceArgument.getRequestBody(), parameterClass));
            }
        }
    }

    private void resolveValuePathVariable(GenericServiceArgument genericServiceArgument, PathVariable pathVariable,
        AtomicReference<Object> returnObjectRef, MethodParameterInfo methodParameterInfo) {
        var argumentMetaModel = methodParameterInfo.getArgumentMetaModel();
        getFirstValue(pathVariable.name(), pathVariable.value(), methodParameterInfo.getName())
            .ifPresent(pathVariableName -> {
                Object pathVariableValue = genericServiceArgument.getUrlPathParams().get(pathVariableName);
                if (pathVariableValue == null) {
                    if (pathVariable.required()) {
                        throw new TechnicalException("Cannot find required path variable value with name: " + pathVariableName);
                    }
                    returnObjectRef.set(NULL_REFERENCE);
                } else {
                    if (argumentMetaModel.getRawClass().isAssignableFrom(pathVariableValue.getClass())) {
                        returnObjectRef.set(pathVariableValue);
                    } else {
                        returnObjectRef.set(jsonObjectMapper.convertToObject(pathVariableValue.toString(), argumentMetaModel));
                    }
                }
            });
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> resolveReturnObject(Object result, EndpointMetaModel endpointMetaModel) {
        var successHttpCodeOptional = Optional.ofNullable(endpointMetaModel.getResponseMetaModel().getSuccessHttpCode());
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

    public void informAboutRequiredAnnotation(Annotation annotation, int parameterIndex, GenericServiceArgument genericServiceArgument) {
        throw new TechnicalException("Argument annotated " + annotation + " is required " + atIndexInMethod(parameterIndex, genericServiceArgument));
    }

    private String atIndexInMethod(int parameterIndex, GenericServiceArgument genericServiceArgument) {
        var method = genericServiceArgument.getEndpointMetaModel().getServiceMetaModel()
            .getMethodMetaModel().getOriginalMethod();
        int realIndex = parameterIndex + 1;
        return String.format("at index: %s%nin class: %s%nwith method name: %s%nin method : %s",
            realIndex, method.getDeclaringClass().getCanonicalName(), method.getName(), method);
    }

    public static Optional<String> getFirstValue(String... arguments) {
        return elements(arguments)
            .filter(StringUtils::isNotBlank)
            .findFirst();
    }

    @Value
    private static class MethodParameterInfo {

        JavaTypeMetaModel argumentMetaModel;
        int index;
        String name;
    }
}
