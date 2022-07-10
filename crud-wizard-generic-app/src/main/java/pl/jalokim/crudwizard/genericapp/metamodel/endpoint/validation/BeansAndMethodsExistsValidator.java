package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration.READ_FIELD_RESOLVER_CONFIG;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel.hasTheSameElementsWithTheSameOrder;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_STRING_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.BeansAndMethodsExistsValidator.InnerError.newError;
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.BeansAndMethodsExistsValidator.TypePredicate.newTypePredicate;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import com.fasterxml.jackson.databind.JsonNode;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.JavaTypeMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService;
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument;
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.crudwizard.genericapp.service.translator.TranslatedPayload;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;
import pl.jalokim.utils.string.StringUtils;

@Component
@RequiredArgsConstructor
// TODO #1 make validation via EndpointMetaModelServiceIT of invalid method arguments, invalid method types, not existed bean, not existed methods.
//  for services and mappers
public class BeansAndMethodsExistsValidator implements BaseConstraintValidator<BeansAndMethodsExists, EndpointMetaModelDto> {

    private final MethodSignatureMetaModelResolver methodSignatureMetaModelResolver;
    private final ClassMetaModelMapper classMetaModelMapper;
    private final ApplicationContext applicationContext;
    private final MetaModelContextService metaModelContextService;

    private static final ClassMetaModelsPredicate METHOD_ARG_IS_SIMPLE_TYPE = (
        methodArgumentMetaModel,
        methodArgClassMetaModel,
        inputTypeOfMapperOrService) -> methodArgClassMetaModel.isSimpleType();

    private static final List<ExpectedMethodArgument> COMMON_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(EndpointMetaModel.class,
            HttpServletRequest.class,
            HttpServletResponse.class),
        argAnnotatedAndAsMetaModels(RequestBody.class,
            newTypePredicate(Object.class, (
                methodArgumentMetaModel,
                methodArgClassMetaModel,
                inputTypeOfMapperOrService) ->
                expectedTypeForRequestBody(methodArgClassMetaModel, inputTypeOfMapperOrService, 0)
            )),
        argAnnotatedAndAsMetaModels(RequestHeader.class,
            newTypePredicate(MAP_STRING_STRING_MODEL),
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestHeader.class).name())
            )),
        argAnnotatedAndAsMetaModels(RequestParam.class,
            newTypePredicate(MAP_STRING_OBJECT_MODEL),
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(RequestParam.class).name()))),
        argAnnotatedAndAsMetaModels(PathVariable.class,
            newTypePredicate(OBJECT_MODEL, METHOD_ARG_IS_SIMPLE_TYPE,
                (
                    methodArgumentMetaModel,
                    methodArgClassMetaModel,
                    inputTypeOfMapperOrService) ->
                    StringUtils.isNotBlank(methodArgumentMetaModel.getParameter().getAnnotation(PathVariable.class).name())))
    );

    @Override
    public boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        ServiceMetaModelDto serviceMetaModel = endpointMetaModelDto.getServiceMetaModel();
        AtomicBoolean isValid = new AtomicBoolean(true);
        validateServiceDefinition(endpointMetaModelDto, context, serviceMetaModel, isValid);
        validateFinalResultMapper(endpointMetaModelDto, context, isValid);
        validateMappersInDataStorageConnectors(endpointMetaModelDto, context, isValid);

        return isValid.get();
    }

    private static final List<ExpectedMethodArgument> SERVICE_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(GenericServiceArgument.class,
            ValidationSessionContext.class,
            JsonNode.class,
            TranslatedPayload.class)
    );

    private static final List<ExpectedMethodArgument> MAPPER_EXPECTED_ARGS_TYPE = List.of(
        argAsTypes(GenericMapperArgument.class),
        argAsPredicates(newTypePredicate(Object.class, (
            methodArgumentMetaModel,
            methodArgClassMetaModel,
            inputTypeOfMapper) -> {
            if (inputTypeOfMapper.isGenericModel() && !inputTypeOfMapper.isSimpleType()) {
                return methodArgClassMetaModel.isMapType()
                    && genericTypeAtIndexIsGivenClass(methodArgClassMetaModel, 0, String.class)
                    && genericTypeAtIndexIsGivenClass(methodArgClassMetaModel, 1, Object.class);
            }
            if (isEmpty(methodArgClassMetaModel.getGenericTypes()) && isEmpty(inputTypeOfMapper.getGenericTypes())) {
                return inputTypeOfMapper.isSubTypeOf(methodArgClassMetaModel);
            } else if (isNotEmpty(methodArgClassMetaModel.getGenericTypes()) && isNotEmpty(inputTypeOfMapper.getGenericTypes())) {
                return inputTypeOfMapper.isTheSameMetaModel(methodArgClassMetaModel);
            }
            return false;
        }))
    );

    private static boolean expectedTypeForRequestBody(ClassMetaModel methodArgClassMetaModel,
        ClassMetaModel inputTypeOfMapperOrService, int genericTypeNestingLevel) {

        if (genericTypeNestingLevel == 0 && Elements.of(JsonNode.class,
            TranslatedPayload.class)
            .anyMatch(givenClass -> givenClass.equals(methodArgClassMetaModel.getRealClass()))) {
            return true;
        } else if (inputTypeOfMapperOrService.isGenericModel()) {
            // raw object not simple, map<String,Object>, not collection and not map
            return methodArgClassMetaModel.equals(MAP_STRING_OBJECT_MODEL)
                || (!methodArgClassMetaModel.isSimpleType()
                && !methodArgClassMetaModel.isCollectionType()
                && !methodArgClassMetaModel.isMapType());
        } else if (inputTypeOfMapperOrService.isGenericMetamodelEnum() || inputTypeOfMapperOrService.isSimpleType()) {
            // simple type
            return methodArgClassMetaModel.isSimpleType();
        } else if (inputTypeOfMapperOrService.isCollectionType()) {
            // some collection
            return methodArgClassMetaModel.isCollectionType()
                && expectedTypeForRequestBody(
                methodArgClassMetaModel.getGenericTypes().get(0),
                inputTypeOfMapperOrService.getGenericTypes().get(0),
                ++genericTypeNestingLevel
            );
        }

        // some object, not simple, not collection
        return !methodArgClassMetaModel.isSimpleType()
            && !methodArgClassMetaModel.isCollectionType()
            && !methodArgClassMetaModel.isMapType();
    }

    private void validateMappersInDataStorageConnectors(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, AtomicBoolean isValid) {

        if (DefaultGenericService.class.equals(getRealClassForServiceBean(endpointMetaModelDto))) {
            elements(endpointMetaModelDto.getDataStorageConnectors())
                .forEachWithIndex((index, dataStorageConnector) -> {

                    var classMetaModelInDataStorage = getClassMetaModelByDto(dataStorageConnector.getClassMetaModelInDataStorage());
                    if (classMetaModelInDataStorage != null) {

                        if (classMetaModelInDataStorage.hasIdField()) {
                            validateMapperForQueryBeanAndMethod(endpointMetaModelDto, context, isValid,
                                index, dataStorageConnector, classMetaModelInDataStorage);
                        } else {
                            isValid.set(false);
                            customMessage(context, createMessagePlaceholder("ClassMetaModel.id.field.not.found",
                                classMetaModelInDataStorage.getName() == null ? "" : classMetaModelInDataStorage.getName()),
                                PropertyPath.builder()
                                    .addNextPropertyAndIndex("dataStorageConnectors", index)
                                    .addNextProperty("classMetaModelInDataStorage")
                                    .build());
                        }

                        validateMapperForPersistBeanAndMethod(endpointMetaModelDto, context, isValid,
                            index, dataStorageConnector, classMetaModelInDataStorage);
                    }
                });
        }
    }

    private void validateMapperForPersistBeanAndMethod(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context,
        AtomicBoolean isValid, Integer index,
        DataStorageConnectorMetaModelDto dataStorageConnector, ClassMetaModel classMetaModelInDataStorage) {

        MapperMetaModelDto mapperMetaModelForPersist = dataStorageConnector.getMapperMetaModelForPersist();

        validateMapperBeanAndMethod(context, isValid, index, mapperMetaModelForPersist,
            getClassMetaModelByDto(endpointMetaModelDto.getPayloadMetamodel()),
            classMetaModelInDataStorage, "mapperMetaModelForPersist");
    }

    private void validateMapperForQueryBeanAndMethod(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, AtomicBoolean isValid, Integer index,
        DataStorageConnectorMetaModelDto dataStorageConnector, ClassMetaModel classMetaModelInDataStorage) {

        FieldMetaModel idFieldMetaModel = classMetaModelInDataStorage.getIdFieldMetaModel();
        MapperMetaModelDto mapperMetaModelForQuery = dataStorageConnector.getMapperMetaModelForQuery();
        UrlMetamodel urlMetamodel = UrlModelResolver.resolveUrl(endpointMetaModelDto.getBaseUrl());

        ClassMetaModel expectedMethodArgumentType = Optional.ofNullable(endpointMetaModelDto.getPathParams())
            .map(this::getClassMetaModelByDto)
            .flatMap(pathParams -> Optional.ofNullable(urlMetamodel.getLastVariableNameInUrl())
                .map(pathParams::getFieldByName))
            .map(FieldMetaModel::getFieldType)
            .orElse(null);

        ClassMetaModel expectedMethodReturnType = idFieldMetaModel.getFieldType();

        validateMapperBeanAndMethod(context, isValid, index, mapperMetaModelForQuery,
            expectedMethodArgumentType, expectedMethodReturnType, "mapperMetaModelForQuery");
    }

    private void validateMapperBeanAndMethod(ConstraintValidatorContext context, AtomicBoolean isValid,
        Integer index, MapperMetaModelDto mapperMetaModelForQuery, ClassMetaModel expectedMethodArgumentType,
        ClassMetaModel expectedMethodReturnType, String mapperFieldName) {

        BeanAndMethodDto mapperBeanAndMethod = Optional.ofNullable(mapperMetaModelForQuery)
            .map(MapperMetaModelDto::getMapperBeanAndMethod)
            .orElse(null);

        if (mapperBeanAndMethod != null && canValidate(mapperBeanAndMethod)) {
            List<InnerError> innerErrors = validateExistenceBeanAndMethod(mapperBeanAndMethod);

            if (isEmpty(innerErrors)
                && canValidateMethodArguments(mapperBeanAndMethod)
                && mapperMetaModelForQuery.getMapperType().equals(MapperType.BEAN_OR_CLASS_NAME)) {

                if (expectedMethodArgumentType != null) {
                    validateMethodArguments(innerErrors, mapperBeanAndMethod,
                        getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE),
                        "BeansAndMethodsExistsValidator.mapper.type",
                        expectedMethodArgumentType
                    );
                }

                if (expectedMethodReturnType != null) {
                    ClassMetaModel methodReturnClassModel = getMethodReturnClassMetaModel(mapperBeanAndMethod);
                    validateMethodReturnType(innerErrors, methodReturnClassModel, expectedMethodReturnType);
                }
            }

            if (isNotEmpty(innerErrors)) {
                for (InnerError innerError : innerErrors) {
                    customMessage(context, innerError.getMessage(),
                        PropertyPath.builder()
                            .addNextPropertyAndIndex("dataStorageConnectors", index)
                            .addNextProperty(mapperFieldName)
                            .addNextProperty(innerError.getPropertyName())
                            .build());
                }
                isValid.set(false);
            }
        }
    }

    private void validateServiceDefinition(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, ServiceMetaModelDto serviceMetaModel,
        AtomicBoolean isValid) {

        if (serviceMetaModel != null) {
            BeanAndMethodDto serviceBeanAndMethod = serviceMetaModel.getServiceBeanAndMethod();
            if (serviceBeanAndMethod != null && canValidate(serviceBeanAndMethod)) {
                List<InnerError> innerErrors = validateExistenceBeanAndMethod(serviceBeanAndMethod);

                if (isEmpty(innerErrors) && canValidateMethodArguments(serviceBeanAndMethod)) {
                    validateMethodArguments(innerErrors, serviceBeanAndMethod,
                        getCommonExpectedArgsTypeAndOther(SERVICE_EXPECTED_ARGS_TYPE),
                        "BeansAndMethodsExistsValidator.service.type",
                        getClassMetaModelByDto(endpointMetaModelDto.getPayloadMetamodel())
                    );

                    validateNotGenericServiceMethodReturnType(innerErrors, endpointMetaModelDto);
                }

                if (isNotEmpty(innerErrors)) {
                    for (InnerError innerError : innerErrors) {
                        customMessage(context, innerError.getMessage(),
                            PropertyPath.builder()
                                .addNextProperty("serviceMetaModel")
                                .addNextProperty("serviceBeanAndMethod")
                                .addNextProperty(innerError.getPropertyName())
                                .build());
                    }
                    isValid.set(false);
                }
            }
        }
    }

    private void validateFinalResultMapper(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, AtomicBoolean isValid) {

        if (DefaultGenericService.class.equals(getRealClassForServiceBean(endpointMetaModelDto))) {

            ClassMetaModel expectedResponseClassModel = getResponseClassModel(endpointMetaModelDto);
            BeanAndMethodDto mapperBeanAndMethod = Optional.ofNullable(endpointMetaModelDto.getResponseMetaModel())
                .map(EndpointResponseMetaModelDto::getMapperMetaModel)
                .map(MapperMetaModelDto::getMapperBeanAndMethod)
                .orElse(null);

            if (mapperBeanAndMethod != null && canValidate(mapperBeanAndMethod)) {
                List<InnerError> innerErrors = validateExistenceBeanAndMethod(mapperBeanAndMethod);

                if (isEmpty(innerErrors) && canValidateMethodArguments(mapperBeanAndMethod)) {

                    if (expectedResponseClassModel != null
                        && endpointMetaModelDto.getHttpMethod().equals(HttpMethod.GET)
                        && endpointMetaModelDto.getResponseMetaModel().getMapperMetaModel().getMapperType()
                        .equals(MapperType.BEAN_OR_CLASS_NAME)) {

                        var dataStorageConnectors = endpointMetaModelDto.getDataStorageConnectors();
                        if (dataStorageConnectors.size() == 1) {
                            DataStorageConnectorMetaModelDto dataStorageConnectorMetaModelDto = dataStorageConnectors.get(0);
                            ClassMetaModel classModelInDataStorage = getClassMetaModelByDto(dataStorageConnectorMetaModelDto
                                .getClassMetaModelInDataStorage());

                            if (classModelInDataStorage != null) {
                                validateMethodArguments(innerErrors, mapperBeanAndMethod,
                                    getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE),
                                    "BeansAndMethodsExistsValidator.mapper.type",
                                    classModelInDataStorage
                                );
                            }
                        } else if (dataStorageConnectors.size() > 1) {
                            validateMethodArguments(innerErrors, mapperBeanAndMethod,
                                getCommonExpectedArgsTypeAndOther(List.of(
                                    argAsTypes(GenericMapperArgument.class, JoinedResultsRow.class))),
                                "BeansAndMethodsExistsValidator.mapper.type",
                                ClassMetaModelFactory.fromRawClass(JoinedResultsRow.class));
                        }

                        ClassMetaModel methodReturnClassModel = getMethodReturnClassMetaModel(mapperBeanAndMethod);

                        if (expectedResponseClassModel.hasRealClass() &&
                            (expectedResponseClassModel.isCollectionType() ||
                                MetadataReflectionUtils.isTypeOf(expectedResponseClassModel.getRealClass(), Page.class))) {
                            expectedResponseClassModel = expectedResponseClassModel.getGenericTypes().get(0);
                        }

                        validateMethodReturnType(innerErrors, methodReturnClassModel, expectedResponseClassModel);
                    }
                }

                if (isNotEmpty(innerErrors)) {
                    for (InnerError innerError : innerErrors) {
                        customMessage(context, innerError.getMessage(),
                            PropertyPath.builder()
                                .addNextProperty("responseMetaModel")
                                .addNextProperty("mapperMetaModel")
                                .addNextProperty("mapperBeanAndMethod")
                                .addNextProperty(innerError.getPropertyName())
                                .build());
                    }
                    isValid.set(false);
                }
            }
        }
    }

    private void validateMethodReturnType(List<InnerError> innerErrors,
        ClassMetaModel methodReturnClassModel, ClassMetaModel expectedResponseClassModel) {

        if (!verifyThatReturnMethodTypeIsAsExpected(
            methodReturnClassModel,
            expectedResponseClassModel)) {
            innerErrors.add(newError("methodName",
                translatePlaceholder("BeansAndMethodsExistsValidator.method.return.type.invalid",
                    methodReturnClassModel.getTypeDescription(),
                    expectedResponseClassModel.getTypeDescription())));
        }
    }

    private boolean canValidate(BeanAndMethodDto beanAndMethodDto) {
        return beanAndMethodDto.getMethodName() != null && beanAndMethodDto.getClassName() != null;
    }

    private void validateMethodArguments(List<InnerError> results,
        BeanAndMethodDto beanAndMethodDto,
        List<ExpectedMethodArgument> methodExpectedArguments,
        String beanTypePropertyKey,
        ClassMetaModel typeOfInputServiceOrMapper) {

        try {
            MethodSignatureMetaModel methodSignatureMetaModel = getMethodSignatureMetaModel(beanAndMethodDto);

            int methodArgIndex = 0;
            for (MethodArgumentMetaModel methodArgument : methodSignatureMetaModel.getMethodArguments()) {
                boolean foundExpectedMethodArgType = false;
                for (ExpectedMethodArgument expectedMethodArgument : methodExpectedArguments) {
                    Class<?> isAnnotatedWith = expectedMethodArgument.getIsAnnotatedWith();
                    List<TypePredicate> typePredicates = expectedMethodArgument.getTypePredicates();

                    if ((isAnnotatedWith == null ||
                        elements(methodArgument.getAnnotations())
                            .map(Annotation::annotationType)
                            .asList()
                            .contains(isAnnotatedWith))
                        && typePredicates.stream()
                        .anyMatch(typePredicate -> {
                                ClassMetaModel classMetaModelFromMethodArg = classMetaModelFromType(
                                    methodArgument.getArgumentType());

                                return (classMetaModelFromMethodArg
                                    .isSubTypeOf(typePredicate.isSubTypeOf) &&
                                    hasTheSameElementsWithTheSameOrder(
                                        classMetaModelFromMethodArg.getGenericTypes(),
                                        typePredicate.isSubTypeOf.getGenericTypes())) &&
                                    (typePredicate.predicatesOfModel.isEmpty() ||
                                        typePredicate.predicatesOfModel.stream().allMatch(
                                            predicateClass -> predicateClass.test(
                                                methodArgument,
                                                classMetaModelFromMethodArg,
                                                typeOfInputServiceOrMapper
                                            )));
                            }
                        )) {
                        foundExpectedMethodArgType = true;
                        break;
                    }
                }
                if (!foundExpectedMethodArgType) {
                    results.add(newError("methodName",
                        translatePlaceholder("BeansAndMethodsExistsValidator.invalid.method.argument",
                            methodArgIndex, wrapAsPlaceholder(beanTypePropertyKey))
                    ));
                }
                methodArgIndex++;
            }
        } catch (TechnicalException ex) {
            // nop
        }
    }

    private MethodSignatureMetaModel getMethodSignatureMetaModel(BeanAndMethodDto beanAndMethodDto) {
        Class<?> beanClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
        Method foundMethod = findMethodByName(beanClass, beanAndMethodDto.getMethodName());
        return methodSignatureMetaModelResolver.resolveMethodSignature(foundMethod, beanClass);
    }

    private ClassMetaModel getMethodReturnClassMetaModel(BeanAndMethodDto beanAndMethodDto) {
        MethodSignatureMetaModel methodSignatureMetaModel = getMethodSignatureMetaModel(beanAndMethodDto);
        return classMetaModelFromType(methodSignatureMetaModel.getReturnType());
    }

    private List<ExpectedMethodArgument> getCommonExpectedArgsTypeAndOther(List<ExpectedMethodArgument> otherExpectedArguments) {
        return elements(COMMON_EXPECTED_ARGS_TYPE)
            .concat(otherExpectedArguments)
            .asList();
    }

    private List<InnerError> validateExistenceBeanAndMethod(BeanAndMethodDto beanAndMethodDto) {
        List<InnerError> results = new ArrayList<>();
        if (beanAndMethodDto.getBeanName() != null) {
            try {
                Object bean = applicationContext.getBean(beanAndMethodDto.getBeanName());
                Class<?> beanClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
                if (!MetadataReflectionUtils.isTypeOf(bean, beanClass)) {
                    results.add(newError("beanName", translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.the.same.class",
                        bean.getClass().getCanonicalName(), beanClass.getCanonicalName())));
                }
                try {
                    findMethodByName(beanClass, beanAndMethodDto.getMethodName());
                } catch (Exception ex) {
                    results.add(newError("methodName", translatePlaceholder("BeansAndMethodsExistsValidator.method.not.found",
                        beanClass.getCanonicalName())));
                }
            } catch (BeansException ex) {
                results.add(newError("beanName", translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.exist")));
            }
        }
        return results;
    }

    private void validateNotGenericServiceMethodReturnType(List<InnerError> innerErrors,
        EndpointMetaModelDto endpointMetaModelDto) {
        BeanAndMethodDto beanAndMethodDto = endpointMetaModelDto.getServiceMetaModel().getServiceBeanAndMethod();
        Class<?> beanClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
        Method foundMethod = findMethodByName(beanClass, beanAndMethodDto.getMethodName());

        if (foundMethod.getAnnotation(GenericMethod.class) == null
            && beanClass.getAnnotation(GenericService.class) == null) {

            ClassMetaModel methodReturnClassModel = getMethodReturnClassMetaModel(beanAndMethodDto);
            if (MetadataReflectionUtils.isTypeOf(methodReturnClassModel.getRealClass(), ResponseEntity.class)) {
                methodReturnClassModel = methodReturnClassModel.getGenericTypes().get(0);
            }

            ClassMetaModel expectedResponseClassModel = getResponseClassModel(endpointMetaModelDto);

            if (expectedResponseClassModel != null) {
                validateMethodReturnType(innerErrors, methodReturnClassModel, expectedResponseClassModel);
            }
        }
    }

    private ClassMetaModel getResponseClassModel(EndpointMetaModelDto endpointMetaModelDto) {
        return Optional.ofNullable(endpointMetaModelDto.getResponseMetaModel())
            .map(EndpointResponseMetaModelDto::getClassMetaModel)
            .map(this::getClassMetaModelByDto)
            .orElse(null);
    }

    private boolean canValidateMethodArguments(BeanAndMethodDto beanAndMethodDto) {
        return beanAndMethodDto.getMethodName() != null && beanAndMethodDto.getClassName() != null;
    }

    private static boolean verifyThatReturnMethodTypeIsAsExpected(ClassMetaModel methodReturnClassMetaModel,
        ClassMetaModel expectedReturnType) {

        if (expectedReturnType.isGenericModel()) {
            return methodReturnClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL);
        } else if (expectedReturnType.isGenericMetamodelEnum()) {
            return methodReturnClassMetaModel.hasRealClass()
                && methodReturnClassMetaModel.getRealClass().equals(String.class);
        } else if (expectedReturnType.isSimpleType()) {
            return methodReturnClassMetaModel.hasRealClass()
                && methodReturnClassMetaModel.getRealClass().equals(expectedReturnType.getRealClass());
        } else if (expectedReturnType.isCollectionType()) {
            return methodReturnClassMetaModel.isCollectionType()
                && !verifyThatReturnMethodTypeIsAsExpected(
                methodReturnClassMetaModel.getGenericTypes().get(0),
                expectedReturnType.getGenericTypes().get(0)
            );
        } else if (expectedReturnType.hasGenericTypes()) {
            return methodReturnClassMetaModel.hasGenericTypes()
                && expectedReturnType.isTheSameMetaModel(methodReturnClassMetaModel);
        }
        return methodReturnClassMetaModel.isSubTypeOf(expectedReturnType);
    }

    @Value
    static class InnerError {

        String propertyName;
        String message;

        static InnerError newError(String propertyName, String message) {
            return new InnerError(propertyName, message);
        }
    }

    @Builder
    @Value
    private static class ExpectedMethodArgument {

        @Builder.Default
        List<TypePredicate> typePredicates = new ArrayList<>();
        Class<? extends Annotation> isAnnotatedWith;
    }

    @Builder
    @Value
    static class TypePredicate {

        @NotNull
        ClassMetaModel isSubTypeOf;
        @Builder.Default
        List<ClassMetaModelsPredicate> predicatesOfModel = List.of();

        static TypePredicate newTypePredicate(Class<?> isSubTypeOf, ClassMetaModelsPredicate... predicateOfType) {
            return newTypePredicate(ClassMetaModel.builder()
                .realClass(isSubTypeOf)
                .build(), predicateOfType);
        }

        static TypePredicate newTypePredicate(ClassMetaModel isSubTypeOf, ClassMetaModelsPredicate... predicateOfType) {
            return TypePredicate.builder()
                .isSubTypeOf(isSubTypeOf)
                .predicatesOfModel(elements(predicateOfType).asList())
                .build();
        }
    }

    private static ExpectedMethodArgument argAsTypes(Class<?>... realClass) {
        return ExpectedMethodArgument.builder()
            .typePredicates(
                elements(realClass)
                    .map(TypePredicate::newTypePredicate)
                    .asList())
            .build();
    }

    private static ExpectedMethodArgument argAsPredicates(TypePredicate... typePredicates) {
        return argAnnotatedAndAsMetaModels(null, typePredicates);
    }

    private static ExpectedMethodArgument argAnnotatedAndAsMetaModels(Class<? extends Annotation> isAnnotatedWith, TypePredicate... typePredicates) {
        return ExpectedMethodArgument.builder()
            .isAnnotatedWith(isAnnotatedWith)
            .typePredicates(elements(typePredicates).asList())
            .build();
    }

    private ClassMetaModel classMetaModelFromType(JavaTypeMetaModel javaTypeMetaModel) {
        TypeMetadata typeMetadata;
        if (javaTypeMetaModel.getOriginalType() != null) {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromType(javaTypeMetaModel.getOriginalType());
        } else {
            typeMetadata = MetadataReflectionUtils.getTypeMetadataFromClass(javaTypeMetaModel.getRawClass());
        }
        return ClassMetaModelFactory.createClassMetaModelFor(typeMetadata, READ_FIELD_RESOLVER_CONFIG, false);
    }

    private interface ClassMetaModelsPredicate {

        boolean test(MethodArgumentMetaModel methodArgumentMetaModel,
            ClassMetaModel methodArgClassMetaModel,
            ClassMetaModel inputTypeOfMapperOrService);
    }

    private static boolean genericTypeAtIndexIsGivenClass(ClassMetaModel classMetaModel, int indexOf, Class<?> expectedRawClass) {
        return expectedRawClass.equals(classMetaModel.getGenericTypes().get(indexOf).getRealClass());
    }

    private ClassMetaModel getClassMetaModelByDto(ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto == null) {
            return null;
        }
        return classMetaModelMapper.toModelFromDto(classMetaModelDto);
    }

    private BeanAndMethodDto getBeanAndMethodDtoFromService(EndpointMetaModelDto endpointMetaModelDto) {
        ServiceMetaModelDto serviceMetaModel = endpointMetaModelDto.getServiceMetaModel();
        if (serviceMetaModel == null) {
            return Optional.ofNullable(metaModelContextService.getMetaModelContext().getDefaultServiceMetaModel()
                .getServiceBeanAndMethod())
                .map(serviceBeanAndMethod -> BeanAndMethodDto.builder()
                    .beanName(serviceBeanAndMethod.getBeanName())
                    .className(serviceBeanAndMethod.getClassName())
                    .methodName(serviceBeanAndMethod.getMethodName())
                    .build())
                .orElse(null);
        }
        return serviceMetaModel.getServiceBeanAndMethod();
    }

    private Class<?> getRealClassForServiceBean(EndpointMetaModelDto endpointMetaModelDto) {
        ServiceMetaModelDto serviceMetaModel = endpointMetaModelDto.getServiceMetaModel();
        if (serviceMetaModel == null) {
            return Optional.ofNullable(metaModelContextService.getMetaModelContext().getDefaultServiceMetaModel()
                .getServiceInstance())
                .map(Object::getClass)
                .map(ClassUtils::loadRealClass)
                .orElse(null);
        } else {
            BeanAndMethodDto serviceBeanAndMethod = serviceMetaModel.getServiceBeanAndMethod();
            if (serviceBeanAndMethod != null) {
                return ClassUtils.loadRealClass(serviceBeanAndMethod.getClassName());
            }
        }
        return null;
    }
}
