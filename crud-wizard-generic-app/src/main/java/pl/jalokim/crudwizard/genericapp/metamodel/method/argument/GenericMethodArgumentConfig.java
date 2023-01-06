package pl.jalokim.crudwizard.genericapp.metamodel.method.argument;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_STRING_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.STRING_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractor.newTypePredicate;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.argument.TypePredicateAndDataExtractor.newTypePredicateAndDataProvide;
import static pl.jalokim.utils.collection.Elements.elements;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@Slf4j
@UtilityClass
@SuppressWarnings({"PMD.GodClass", "PMD.CouplingBetweenObjects"})
public class GenericMethodArgumentConfig {

    public static final Object NULL_REFERENCE = new Object();

    public static final List<GenericMethodArgument> SERVICE_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(
            classAndDataExtractor(GenericServiceArgument.class, GenericServiceArgumentMethodProvider::getGenericServiceArgument),
            classAndDataExtractor(ValidationSessionContext.class, GenericServiceArgumentMethodProvider::getValidationSessionContext),
            classAndDataExtractor(JsonNode.class, GenericServiceArgumentMethodProvider::getRequestBodyAsJsonNode),
            classAndDataExtractor(TranslatedPayload.class, GenericServiceArgumentMethodProvider::getTranslatedPayload)
        ));

    public static final List<GenericMethodArgument> MAPPER_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(classAndDataExtractor(GenericMapperArgument.class, GenericMapperArgumentMethodProvider::getGenericMapperArgument)),
        argAsPredicates(newTypePredicateAndDataProvide(Object.class,
            GenericMapperArgumentMethodProvider::getMapperInput,
            (
                methodArgumentMetaModel,
                methodArgClassMetaModel,
                inputTypeOfMapper,
                classMetamodelForAnnotation) -> canBeAssignAsMapperArgument(methodArgClassMetaModel, inputTypeOfMapper)
        )).toBuilder()
            .argumentCanBeInputOfMapper(true)
            .build()
    );

    public static final List<GenericMethodArgument> MAPPER_JOINED_RESULTS_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(classAndDataExtractor(GenericMapperArgument.class, GenericMapperArgumentMethodProvider::getGenericMapperArgument)),
        argAsTypes(classAndDataExtractor(JoinedResultsRow.class, GenericMapperArgumentMethodProvider::getMapperInput))
    );

    private static final ClassMetaModelsPredicate METHOD_ARG_IS_SIMPLE_TYPE = (
        methodArgumentMetaModel,
        methodArgClassMetaModel,
        inputTypeOfMapperOrService,
        endpointQueryAndUrlMetaModel) -> methodArgClassMetaModel.isSimpleType();

    private static final List<GenericMethodArgument> COMMON_EXPECTED_ARGS_TYPE = List.of(argAsTypes(
        classAndDataExtractor(EndpointMetaModel.class, GenericMethodArgumentProvider::getEndpointMetaModel),
        classAndDataExtractor(HttpServletRequest.class, GenericMethodArgumentProvider::getHttpServletRequest),
        classAndDataExtractor(HttpServletResponse.class, GenericMethodArgumentProvider::getHttpServletResponse)),
        argAnnotatedAndAsMetaModels(RequestBody.class,
            GenericMethodArgumentConfig::validateRequestBodyAnnotation,
            newTypePredicateAndDataProvide(JsonNode.class, GenericMethodArgumentProvider::getRequestBodyAsJsonNode),
            newTypePredicateAndDataProvide(TranslatedPayload.class, GenericMethodArgumentProvider::getTranslatedPayload),
            newTypePredicateAndDataProvide(String.class, getJsonNodeAsString()),
            newTypePredicate(Object.class,
                GenericMethodArgumentConfig::getRequestBody,
                validateMethodSignatureMatchExpectedModels()
            )),
        argAnnotatedAndAsMetaModels(RequestHeader.class,
            null,
            newTypePredicate(MAP_STRING_OBJECT_MODEL, GenericMethodArgumentConfig::resolveHeadersAsMap),
            newTypePredicate(MAP_STRING_STRING_MODEL, GenericMethodArgumentConfig::resolveHeadersAsMap),
            newTypePredicate(OBJECT_MODEL, GenericMethodArgumentConfig::resolveHeaderAsSimpleValue, METHOD_ARG_IS_SIMPLE_TYPE)
        ),
        argAnnotatedAndAsMetaModels(RequestParam.class,
            null,
            newTypePredicate(MAP_STRING_OBJECT_MODEL, GenericMethodArgumentConfig::resolveQueryParamsAsMap),
            newTypePredicate(OBJECT_MODEL, GenericMethodArgumentConfig::resolveQueryParamAsSimpleValue,
                METHOD_ARG_IS_SIMPLE_TYPE,
                validateRequestParamFieldName()
            )),
        argAnnotatedAndAsMetaModels(PathVariable.class,
            GenericMethodArgumentConfig::validateResolvedPathVariable,
            newTypePredicate(OBJECT_MODEL,
                GenericMethodArgumentConfig::resolvePathVariableAsSimpleValue,
                METHOD_ARG_IS_SIMPLE_TYPE,
                validatePathVariableFieldName()))
    );

    private static Function<GenericMethodArgumentProvider, Object> getJsonNodeAsString() {
        return genericMethodArgumentProvider -> Optional.ofNullable(genericMethodArgumentProvider.getRequestBodyAsJsonNode())
            .map(JsonNode::toString)
            .orElse(null);
    }

    public static List<GenericMethodArgument> getCommonExpectedArgsTypeAndOther(List<GenericMethodArgument> otherExpectedArguments) {
        return elements(COMMON_EXPECTED_ARGS_TYPE)
            .concat(otherExpectedArguments)
            .asList();
    }

    private static GenericMethodArgument argAsTypes(ClassAndDataExtractor... classAndDataExtractors) {
        return GenericMethodArgument.builder()
            .typePredicatesAndDataExtractors(
                elements(classAndDataExtractors)
                    .map(classAndDataExtractor -> newTypePredicate(classAndDataExtractor.getRealClass(),
                        argument -> classAndDataExtractor.getExtractDataFunction()
                            .apply(argument.getGenericMethodArgumentProvider())))
                    .asList())
            .build();
    }

    private static ClassMetaModelsPredicate validateMethodSignatureMatchExpectedModels() {
        return (methodArgumentMetaModel,
            methodArgClassMetaModel,
            inputTypeOfMapperOrService,
            endpointQueryAndUrlMetaModel) ->
            expectedTypeForRequestBody(methodArgClassMetaModel, inputTypeOfMapperOrService, 0);
    }

    private static ClassMetaModelsPredicate validatePathVariableFieldName() {
        return (
            methodArgumentMetaModel,
            methodArgClassMetaModel,
            inputTypeOfMapperOrService,
            endpointQueryAndUrlMetaModel) -> {
            PathVariable pathVariable = methodArgumentMetaModel.getParameter().getAnnotation(PathVariable.class);
            return isValidFieldName(methodArgumentMetaModel, endpointQueryAndUrlMetaModel.getPathParamsModel(),
                pathVariable.name(), pathVariable.value());
        };
    }

    private static ClassMetaModelsPredicate validateRequestParamFieldName() {
        return (
            methodArgumentMetaModel,
            methodArgClassMetaModel,
            inputTypeOfMapperOrService,
            endpointQueryAndUrlMetaModel) -> {
            RequestParam requestParam = methodArgumentMetaModel.getParameter().getAnnotation(RequestParam.class);
            return isValidFieldName(methodArgumentMetaModel, endpointQueryAndUrlMetaModel.getQueryArgumentsModel(),
                requestParam.name(), requestParam.value());
        };
    }

    private static Object validateResolvedPathVariable(ResolvedValueForAnnotation resolvedValueForAnnotation) {
        PathVariable pathVariable = resolvedValueForAnnotation.getExpectedAnnotation();
        MethodParameterInfo methodParameterInfo = resolvedValueForAnnotation.getMethodParameterInfo();
        String pathVariableName = getFirstValue(pathVariable.name(), pathVariable.value(), methodParameterInfo.getName()).get();
        if (resolvedValueForAnnotation.isValueNotResolved()) {
            if (pathVariable.required()) {
                throw new TechnicalException("Cannot find required path variable value with name: " + pathVariableName);
            } else {
                return NULL_REFERENCE;
            }
        }
        return resolvedValueForAnnotation.getResolvedValue();
    }

    private static Boolean isValidFieldName(MethodArgumentMetaModel methodArgumentMetaModel,
        ClassMetaModel inputTypeOfMapperOrService, String nameFromAnnotation, String valueFromAnnotation) {

        if (inputTypeOfMapperOrService == null) {
            return false;
        }

        String parameterName = methodArgumentMetaModel.getParameter().getName();
        return getFirstValue(nameFromAnnotation, valueFromAnnotation, parameterName)
            .map(inputTypeOfMapperOrService::getFieldByName)
            .map(Objects::nonNull)
            .orElse(false);
    }

    private static Object validateRequestBodyAnnotation(ResolvedValueForAnnotation resolvedValueForAnnotation) {
        if (resolvedValueForAnnotation.isValueNotResolved()) {
            RequestBody requestBodyAnnotation = resolvedValueForAnnotation.getExpectedAnnotation();
            if (requestBodyAnnotation.required()) {
                informAboutRequiredAnnotation(resolvedValueForAnnotation.getMethodParameterAndAnnotation());
            } else {
                return NULL_REFERENCE;
            }
        }
        return resolvedValueForAnnotation.getResolvedValue();
    }

    @SuppressWarnings({"PMD.AvoidReassigningParameters", "PMD.CognitiveComplexity"})
    private static boolean expectedTypeForRequestBody(ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel inputTypeOfMapperOrService, int genericTypeNestingLevel) {

        log.debug("methodArgClassMetaModel: {}, inputTypeOfMapperOrService: {}",
            methodArgClassMetaModel.getTypeDescription(),
            inputTypeOfMapperOrService.getTypeDescription());

        if (genericTypeNestingLevel == 0 && Elements.of(JsonNode.class,
            TranslatedPayload.class)
            .anyMatch(givenClass -> givenClass.equals(methodArgClassMetaModel.getRealClass()))) {
            log.debug("methodArgClassMetaModel is JsonNode or TranslatedPayload");
            return true;
        } else if (inputTypeOfMapperOrService.isGenericMetamodelEnum()) {
            boolean result = methodArgClassMetaModel.isSubTypeOf(STRING_MODEL);
            log.debug("when inputTypeOfMapperOrService is generic meta model, methodArgClassMetaModel is a String when, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isGenericModel()) {
            boolean result = methodArgClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL) ||
                !methodArgClassMetaModel.isSimpleType() &&
                    !methodArgClassMetaModel.isCollectionType() &&
                    !methodArgClassMetaModel.isMapType();
            log.debug("methodArgClassMetaModel is generic object, not simple, not map<String,Object>, not collection and not map, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isSimpleType()) {
            boolean result = methodArgClassMetaModel.isSimpleType();
            log.debug("methodArgClassMetaModel is simple, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isCollectionType()) {
            boolean result = methodArgClassMetaModel.isCollectionType() &&
                expectedTypeForRequestBody(
                    methodArgClassMetaModel.getGenericTypes().get(0),
                    inputTypeOfMapperOrService.getGenericTypes().get(0),
                    ++genericTypeNestingLevel
                );
            log.debug("methodArgClassMetaModel is collection, result: {}", result);
            return result;
        } else if (inputTypeOfMapperOrService.isMapType()) {
            boolean result = methodArgClassMetaModel.isMapType() &&
                expectedTypeForRequestBody(
                    methodArgClassMetaModel.getGenericTypes().get(0),
                    inputTypeOfMapperOrService.getGenericTypes().get(0),
                    ++genericTypeNestingLevel
                ) && expectedTypeForRequestBody(
                methodArgClassMetaModel.getGenericTypes().get(1),
                inputTypeOfMapperOrService.getGenericTypes().get(1),
                genericTypeNestingLevel
            );
            log.debug("methodArgClassMetaModel is map, result: {}", result);
            return result;
        }

        boolean result = !methodArgClassMetaModel.isSimpleType() &&
            !methodArgClassMetaModel.isCollectionType() &&
            !methodArgClassMetaModel.isMapType();
        log.debug("methodArgClassMetaModel is not simple, not collection, not map, result: {}", result);
        return result;
    }

    private static boolean canBeAssignAsMapperArgument(ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel expectedInputTypeOfMapper) {

        log.debug("methodArgClassMetaModel: {}, expectedInputTypeOfMapper: {}",
            methodArgClassMetaModel.getTypeDescription(),
            expectedInputTypeOfMapper.getTypeDescription());

        if (expectedInputTypeOfMapper.isGenericMetamodelEnum()) {
            boolean result = methodArgClassMetaModel.isSubTypeOf(STRING_MODEL);
            log.debug("expectedInputTypeOfMapper is generic enum but methodArgClassMetaModel is type of String, result: {}", result);
            return result;
        } else if (expectedInputTypeOfMapper.isGenericModel()) {
            boolean result = methodArgClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL) ||
                MAP_STRING_OBJECT_MODEL.isSubTypeOf(methodArgClassMetaModel);
            log.debug("expectedInputTypeOfMapper is generic metamodel but methodArgClassMetaModel is type of Map<String, Object> " +
                "or Map is sub type of methodArgClassMetaModel, result: {}", result);
            return result;
        } else if (expectedInputTypeOfMapper.hasGenericTypes()) {

            boolean result = methodArgClassMetaModel.hasGenericTypes() &&
                expectedInputTypeOfMapper.getGenericTypes().size() == methodArgClassMetaModel.getGenericTypes().size();

            log.debug("methodArgClassMetaModel has generic types and the same size like expectedInputTypeOfMapper, result: {}", result);

            AtomicBoolean matchAll = new AtomicBoolean(true);
            if (result) {
                var methodGenericTypes = methodArgClassMetaModel.getGenericTypes();
                var expectedGenericTypes = expectedInputTypeOfMapper.getGenericTypes();
                elements(methodGenericTypes).forEachWithIndex((index, methodArgumentType) ->
                    matchAll.set(matchAll.get() && canBeAssignAsMapperArgument(methodArgumentType, expectedGenericTypes.get(index)))
                );
            }
            log.debug("generic types of expectedInputTypeOfMapper and methodArgClassMetaModel are the same, result: {}", matchAll);

            return result && matchAll.get();
        }

        boolean result = expectedInputTypeOfMapper.isSubTypeOf(methodArgClassMetaModel);
        log.debug("expectedInputTypeOfMapper is sub type of methodArgClassMetaModel, result: {}", result);
        return result;
    }

    private static GenericMethodArgument argAsPredicates(TypePredicateAndDataExtractor... typePredicates) {
        return argAnnotatedAndAsMetaModels(null, null, typePredicates);
    }

    private static GenericMethodArgument argAnnotatedAndAsMetaModels(Class<? extends Annotation> isAnnotatedWith,
        Function<ResolvedValueForAnnotation, Object> resolvedValueValidator,
        TypePredicateAndDataExtractor... typePredicates) {
        return GenericMethodArgument.builder()
            .annotatedWith(isAnnotatedWith)
            .resolvedValueValidator(resolvedValueValidator)
            .typePredicatesAndDataExtractors(elements(typePredicates).asList())
            .build();
    }

    @Value
    private static class ClassAndDataExtractor {

        Class<?> realClass;
        Function<GenericMethodArgumentProvider, Object> extractDataFunction;
    }

    @SuppressWarnings("unchecked")
    private static <P extends GenericMethodArgumentProvider> ClassAndDataExtractor classAndDataExtractor(Class<?> realClass,
        Function<P, Object> extractDataFunction) {
        return new ClassAndDataExtractor(realClass, (Function<GenericMethodArgumentProvider, Object>) extractDataFunction);
    }

    public void informAboutRequiredAnnotation(MethodParameterAndAnnotation methodParameterAndAnnotation) {
        Annotation annotation = methodParameterAndAnnotation.getAnnotation();
        MethodParameterInfo methodParameterInfo = methodParameterAndAnnotation.getMethodParameterInfo();
        throw new TechnicalException("Argument annotated " + annotation + " is required " +
            atIndexInMethod(methodParameterInfo.getIndex(), methodParameterAndAnnotation.getForMethod()));
    }

    private String atIndexInMethod(int parameterIndex, Method method) {
        int realIndex = parameterIndex + 1;
        return String.format("at index: %s%nin class: %s%nwith method name: %s%nin method : %s",
            realIndex, method.getDeclaringClass().getCanonicalName(), method.getName(), method);
    }

    private static Object getRequestBody(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        var genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        var methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        JsonNode jsonNode = genericMethodArgumentProvider.getRequestBodyAsJsonNode();
        if (jsonNode != null) {
            MethodParameterInfo methodParameterInfo = methodParameterAndAnnotation.getMethodParameterInfo();
            return JsonObjectMapper.getInstance().convertToObject(jsonNode.toString(), methodParameterInfo.getArgumentMetaModel());
        }
        return null;
    }

    private static Object resolveHeadersAsMap(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        GenericMethodArgumentProvider genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        MethodParameterAndAnnotation methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        RequestHeader requestHeader = methodParameterAndAnnotation.getExpectedAnnotation();
        Map<String, String> headers = genericMethodArgumentProvider.getHeaders();
        if (headers == null) {
            if (requestHeader.required()) {
                informAboutRequiredAnnotation(methodParameterAndAnnotation);
            }
            return NULL_REFERENCE;
        }
        return headers;
    }

    private static Object resolveHeaderAsSimpleValue(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        GenericMethodArgumentProvider genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        MethodParameterAndAnnotation methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        MethodParameterInfo methodParameterInfo = methodParameterAndAnnotation.getMethodParameterInfo();
        RequestHeader requestHeader = methodParameterAndAnnotation.getExpectedAnnotation();
        Map<String, String> nullableHeaders = genericMethodArgumentProvider.getHeaders();
        return getFirstValue(requestHeader.name(), requestHeader.value(), methodParameterInfo.getName())
            .map(headerName -> {

                String headerValue = Optional.ofNullable(nullableHeaders)
                    .map(headers -> headers.get(headerName))
                    .orElse(null);

                if (headerValue == null) {
                    if (requestHeader.required()) {
                        throw new TechnicalException("Cannot find required header value with header name: " + headerName);
                    }
                    return NULL_REFERENCE;
                } else {
                    return returnTheSameObjectOrMap(methodParameterInfo, headerValue);
                }
            })
            .orElse(null);
    }

    private static Object resolveQueryParamsAsMap(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        GenericMethodArgumentProvider genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        MethodParameterAndAnnotation methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        RequestParam requestParam = methodParameterAndAnnotation.getExpectedAnnotation();
        Map<String, Object> httpQueryTranslated = genericMethodArgumentProvider.getHttpQueryTranslated();
        if (httpQueryTranslated == null) {
            if (requestParam.required()) {
                informAboutRequiredAnnotation(methodParameterAndAnnotation);
            }
            return NULL_REFERENCE;
        }
        return httpQueryTranslated;
    }

    private static Object resolveQueryParamAsSimpleValue(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        GenericMethodArgumentProvider genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        MethodParameterAndAnnotation methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        MethodParameterInfo methodParameterInfo = methodParameterAndAnnotation.getMethodParameterInfo();
        RequestParam requestParam = methodParameterAndAnnotation.getExpectedAnnotation();
        Map<String, Object> httpQueryTranslated = genericMethodArgumentProvider.getHttpQueryTranslated();
        return getFirstValue(requestParam.name(), requestParam.value(), methodParameterInfo.getName())
            .map(requestParamName -> {
                Object requestParamValue = httpQueryTranslated.get(requestParamName);
                if (requestParamValue == null) {
                    if (requestParam.required()) {
                        throw new TechnicalException("Cannot find required http request parameter with name: " + requestParamName);
                    }
                    return NULL_REFERENCE;
                } else {
                    return returnTheSameObjectOrMap(methodParameterInfo, requestParamValue);
                }
            })
            .orElse(null);
    }

    private static Object resolvePathVariableAsSimpleValue(ArgumentValueExtractMetaModel argumentValueExtractMetaModel) {
        GenericMethodArgumentProvider genericMethodArgumentProvider = argumentValueExtractMetaModel.getGenericMethodArgumentProvider();
        MethodParameterAndAnnotation methodParameterAndAnnotation = argumentValueExtractMetaModel.getMethodParameterAndAnnotation();
        MethodParameterInfo methodParameterInfo = methodParameterAndAnnotation.getMethodParameterInfo();
        PathVariable pathVariable = methodParameterAndAnnotation.getExpectedAnnotation();
        Map<String, Object> urlPathParams = genericMethodArgumentProvider.getUrlPathParams();
        return getFirstValue(pathVariable.name(), pathVariable.value(), methodParameterInfo.getName())
            .map(urlPathParams::get)
            .map(pathVariableValue -> returnTheSameObjectOrMap(methodParameterInfo, pathVariableValue))
            .orElse(null);
    }

    private static Optional<String> getFirstValue(String... arguments) {
        return elements(arguments)
            .filter(StringUtils::isNotBlank)
            .findFirst();
    }

    private static Object returnTheSameObjectOrMap(MethodParameterInfo methodParameterInfo, Object resolvedValue) {
        JavaTypeMetaModel argumentMetaModel = methodParameterInfo.getArgumentMetaModel();
        if (argumentMetaModel.getRawClass().isAssignableFrom(resolvedValue.getClass())) {
            return resolvedValue;
        } else {
            return JsonObjectMapper.getInstance().convertToObject(resolvedValue.toString(), argumentMetaModel);
        }
    }
}
