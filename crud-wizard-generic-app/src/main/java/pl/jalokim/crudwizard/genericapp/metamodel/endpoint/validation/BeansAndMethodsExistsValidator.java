package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.checkThatClassExists;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.clearCglibClassName;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.findMethodByName;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelConstants.MAP_STRING_OBJECT_MODEL;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.classMetaModelFromType;
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.BeansAndMethodsExistsValidator.InnerError.newError;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.MAPPER_EXPECTED_ARGS_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.SERVICE_EXPECTED_ARGS_TYPE;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.argAsTypes;
import static pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.getCommonExpectedArgsTypeAndOther;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;
import pl.jalokim.crudwizard.genericapp.mapper.DefaultGenericMapper;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;
import pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.ExpectedMethodArgument;
import pl.jalokim.crudwizard.genericapp.metamodel.method.ExpectedMethodArgumentConfig.TypePredicate;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodArgumentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.method.MethodSignatureMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlMetamodel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService;
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeansAndMethodsExistsValidator implements BaseConstraintValidator<BeansAndMethodsExists, EndpointMetaModelDto> {

    private final MethodSignatureMetaModelResolver methodSignatureMetaModelResolver;
    private final ClassMetaModelMapper classMetaModelMapper;
    private final ApplicationContext applicationContext;
    private final MetaModelContextService metaModelContextService;

    /*
        payloads:                   valid as java type
            generic_model,          Map<String,Object>|someDto not simple
            list<generic_model>,    List<Map<String,Object>>|List<someDto not simple>
            realDto,                someDto not simple
            list<realDto>,          list<realDto>
            long,                   every simple type
            generic_enum,           every simple type
            enum,                   every simple type
            Map<String, Long>       Map<String, Long>

        method return types and arguments:
            generic_model -> Map<String,Object>
            generic_enum -> String
            Number -> Long, Double, Integer, Number
            List<generic_model> -> List<Map<String,Object>>
            Map<String, generic_model> -> Map<String, Map<String, Object>
        */

    @Override
    public boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        ServiceMetaModelDto serviceMetaModel = endpointMetaModelDto.getServiceMetaModel();
        AtomicBoolean isValid = new AtomicBoolean(true);
        validateServiceDefinition(endpointMetaModelDto, context, serviceMetaModel, isValid);
        validateFinalResultMapper(endpointMetaModelDto, context, isValid);
        validateMappersInDataStorageConnectors(endpointMetaModelDto, context, isValid);

        return isValid.get();
    }

    private void validateMappersInDataStorageConnectors(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, AtomicBoolean isValid) {

        if (DefaultGenericService.class.equals(getRealClassForServiceBean(endpointMetaModelDto))) {
            elements(endpointMetaModelDto.getDataStorageConnectors())
                .forEachWithIndex((index, dataStorageConnector) -> {

                    var classMetaModelInDataStorage = getClassMetaModelByDto(dataStorageConnector.getClassMetaModelInDataStorage());
                    if (classMetaModelInDataStorage != null) {

                        if (!classMetaModelInDataStorage.hasIdField()) {

                            String classModelDescribe = "";

                            if (classMetaModelInDataStorage.isGenericModel()) {
                                classModelDescribe = classMetaModelInDataStorage.getName();
                            } else if (classMetaModelInDataStorage.hasRealClass()) {
                                classModelDescribe = classMetaModelInDataStorage.getJavaGenericTypeInfo();
                            }

                            isValid.set(false);
                            customMessage(context, createMessagePlaceholder("ClassMetaModel.id.field.not.found",
                                classModelDescribe),
                                PropertyPath.builder()
                                    .addNextPropertyAndIndex("dataStorageConnectors", index)
                                    .addNextProperty("classMetaModelInDataStorage")
                                    .build());
                        }

                        if (classMetaModelInDataStorage.hasIdField() && HttpMethod.GET.equals(endpointMetaModelDto.getHttpMethod())) {
                            validateMapperForQueryBeanAndMethod(endpointMetaModelDto, context, isValid,
                                index, dataStorageConnector, classMetaModelInDataStorage);
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

        log.debug("validateMapperForPersistBeanAndMethod");

        MapperMetaModelDto mapperMetaModelForPersist = dataStorageConnector.getMapperMetaModelForPersist();

        validateMapperBeanAndMethod(context, isValid, index, mapperMetaModelForPersist,
            getClassMetaModelByDto(endpointMetaModelDto.getPayloadMetamodel()),
            classMetaModelInDataStorage, "mapperMetaModelForPersist");
    }

    private void validateMapperForQueryBeanAndMethod(EndpointMetaModelDto endpointMetaModelDto,
        ConstraintValidatorContext context, AtomicBoolean isValid, Integer index,
        DataStorageConnectorMetaModelDto dataStorageConnector, ClassMetaModel classMetaModelInDataStorage) {

        log.debug("validateMapperForQueryBeanAndMethod");

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
        Integer index, MapperMetaModelDto mapperMetaModel, ClassMetaModel expectedMethodArgumentType,
        ClassMetaModel expectedMethodReturnType, String mapperFieldName) {

        BeanAndMethodDto mapperBeanAndMethod = Optional.ofNullable(mapperMetaModel)
            .map(MapperMetaModelDto::getMapperBeanAndMethod)
            .orElse(null);

        if (mapperBeanAndMethod != null && canValidate(mapperBeanAndMethod) && isNotDefaultGenericMapper(mapperBeanAndMethod)) {
            List<InnerError> innerErrors = validateExistenceBeanAndMethod(mapperBeanAndMethod);

            if (isEmpty(innerErrors)
                && canValidateMethodArguments(mapperBeanAndMethod)
                && mapperMetaModel.getMapperType().equals(MapperType.BEAN_OR_CLASS_NAME)) {

                if (expectedMethodArgumentType != null) {
                    validateMethodArguments(innerErrors, mapperBeanAndMethod,
                        getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE),
                        "BeansAndMethodsExistsValidator.mapper.type",
                        expectedMethodArgumentType
                    );
                }

                if (expectedMethodReturnType != null) {
                    ClassMetaModel methodReturnClassModel = methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(mapperBeanAndMethod);
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

            if (mapperBeanAndMethod != null && canValidate(mapperBeanAndMethod) && isNotDefaultGenericMapper(mapperBeanAndMethod)) {
                List<InnerError> innerErrors = validateExistenceBeanAndMethod(mapperBeanAndMethod);

                if (isEmpty(innerErrors) && canValidateMethodArguments(mapperBeanAndMethod)) {

                    if (expectedResponseClassModel != null
                        && endpointMetaModelDto.getResponseMetaModel().getMapperMetaModel().getMapperType()
                        .equals(MapperType.BEAN_OR_CLASS_NAME)) {
                        if (endpointMetaModelDto.getHttpMethod().equals(HttpMethod.GET)) {

                            var dataStorageConnectors = endpointMetaModelDto.getDataStorageConnectors();

                            ClassMetaModel methodReturnClassModel = methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(mapperBeanAndMethod);

                            boolean endpointReturnsListOrPage = false;
                            if (expectedResponseClassModel.hasRealClass() &&
                                (expectedResponseClassModel.isCollectionType() ||
                                    MetadataReflectionUtils.isTypeOf(expectedResponseClassModel.getRealClass(), Page.class))) {
                                expectedResponseClassModel = expectedResponseClassModel.getGenericTypes().get(0);
                                endpointReturnsListOrPage = true;
                            }

                            if (dataStorageConnectors.size() > 1 && endpointReturnsListOrPage) {
                                validateMethodArguments(innerErrors, mapperBeanAndMethod,
                                    getCommonExpectedArgsTypeAndOther(List.of(
                                        argAsTypes(GenericMapperArgument.class, JoinedResultsRow.class))),
                                    "BeansAndMethodsExistsValidator.mapper.type",
                                    ClassMetaModelFactory.fromRawClass(JoinedResultsRow.class));
                            } else if (dataStorageConnectors.size() == 1) {
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
                            }

                            validateMethodReturnType(innerErrors, methodReturnClassModel, expectedResponseClassModel);
                        } else {
                            validateMethodArguments(innerErrors, mapperBeanAndMethod,
                                getCommonExpectedArgsTypeAndOther(MAPPER_EXPECTED_ARGS_TYPE),
                                "BeansAndMethodsExistsValidator.mapper.type",
                                MAP_STRING_OBJECT_MODEL);

                            ClassMetaModel methodReturnClassModel = methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(mapperBeanAndMethod);
                            validateMethodReturnType(innerErrors, methodReturnClassModel, expectedResponseClassModel);
                        }
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

        log.debug("validateMethodReturnType");

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
            log.debug("validateMethodArguments");
            log.debug("bean type: {}, method name: {}", beanAndMethodDto.getClassName(), beanAndMethodDto.getMethodName());
            MethodSignatureMetaModel methodSignatureMetaModel = methodSignatureMetaModelResolver.getMethodSignatureMetaModel(beanAndMethodDto);

            int methodArgIndex = 0;
            for (MethodArgumentMetaModel methodArgument : methodSignatureMetaModel.getMethodArguments()) {
                log.debug("argument index: {} name: {}", methodArgIndex, methodArgument.getParameter().getName());
                ClassMetaModel classMetaModelFromMethodArg = classMetaModelFromType(methodArgument.getArgumentType());
                log.debug("classMetaModelFromMethodArg: {}", classMetaModelFromMethodArg.getTypeDescription());
                boolean foundExpectedMethodArgType = false;
                for (ExpectedMethodArgument expectedMethodArgument : methodExpectedArguments) {

                    Class<?> isAnnotatedWith = expectedMethodArgument.getIsAnnotatedWith();
                    List<TypePredicate> typePredicates = expectedMethodArgument.getTypePredicates();
                    log.debug("expectedMethodArgument isAnnotatedWith: {}", isAnnotatedWith);
                    log.debug("expectedMethodArgument typePredicates size: {}", typePredicates.size());

                    boolean isAnnotatedWithFound = false;
                    AtomicInteger index = new AtomicInteger();
                    if ((isAnnotatedWith == null ||
                        (isAnnotatedWithFound = elements(methodArgument.getAnnotations())
                            .map(Annotation::annotationType)
                            .asList()
                            .contains(isAnnotatedWith)))
                        && elements(typePredicates)
                        .anyMatch(typePredicate -> {
                                log.debug("checking predicate at index: {}", index.incrementAndGet());
                                boolean subTypeOfResult = classMetaModelFromMethodArg
                                    .isSubTypeOf(typePredicate.getIsSubTypeOf());

                                log.debug("classMetaModelFromMethodArg is sub type of {}, result: {}",
                                    typePredicate.getIsSubTypeOf().getTypeDescription(), subTypeOfResult);

                                boolean predicatesAllEmptyOrAllMatch = typePredicate.getPredicatesOfModel().isEmpty() ||
                                    typePredicate.getPredicatesOfModel().stream().allMatch(
                                        predicateClass -> predicateClass.test(
                                            methodArgument,
                                            classMetaModelFromMethodArg,
                                            typeOfInputServiceOrMapper
                                        ));

                                log.debug("predicatesAllEmptyOrAllMatch: {}", predicatesAllEmptyOrAllMatch);

                                return subTypeOfResult &&
                                    predicatesAllEmptyOrAllMatch;
                            }
                        )) {
                        log.debug("foundExpectedMethodArgType set to true");
                        foundExpectedMethodArgType = true;
                        break;
                    }

                    if (isAnnotatedWithFound) {
                        log.debug("foundExpectedMethodArgType set to false due to isAnnotatedWithFound=true");
                        foundExpectedMethodArgType = false;
                        break;
                    }
                }
                if (!foundExpectedMethodArgType) {
                    log.debug("validateMethodArguments not found foundExpectedMethodArgType");
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

    private List<InnerError> validateExistenceBeanAndMethod(BeanAndMethodDto beanAndMethodDto) {
        List<InnerError> results = new ArrayList<>();

        try {
            Class<?> beanClass = ClassUtils.loadRealClass(beanAndMethodDto.getClassName());
            if (beanAndMethodDto.getBeanName() != null) {
                Object bean = applicationContext.getBean(beanAndMethodDto.getBeanName());
                if (!MetadataReflectionUtils.isTypeOf(bean, beanClass)) {
                    results.add(newError("beanName", translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.the.same.class",
                        clearCglibClassName(bean.getClass().getCanonicalName()), beanClass.getCanonicalName())));
                }
            }
            try {
                findMethodByName(beanClass, beanAndMethodDto.getMethodName());
            } catch (Exception ex) {
                results.add(newError("methodName", translatePlaceholder("BeansAndMethodsExistsValidator.method.not.found",
                    beanClass.getCanonicalName())));
            }
        } catch (BeansException ex) {
            results.add(newError("beanName", translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.exist")));
        } catch (ReflectionOperationException ex) {
            if (ex.getCause() instanceof ClassNotFoundException) {
                results.add(newError("className",
                    createMessagePlaceholder(buildMessageForValidator(ClassExists.class),
                        Map.of("expectedOfType", Object.class.getCanonicalName()))
                        .translateMessage()));
            } else {
                throw ex;
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

            ClassMetaModel methodReturnClassModel = methodSignatureMetaModelResolver.getMethodReturnClassMetaModel(beanAndMethodDto);
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

        log.debug("methodReturnClassMetaModel: {}, expectedReturnType: {}",
            methodReturnClassMetaModel.getTypeDescription(), expectedReturnType.getTypeDescription());

        if (expectedReturnType.isGenericMetamodelEnum()) {
            boolean result = methodReturnClassMetaModel.hasRealClass()
                && methodReturnClassMetaModel.getRealClass().equals(String.class);
            log.debug("methodReturnClassMetaModel is String type, result: {}", result);
            return result;
        } else if (expectedReturnType.isGenericModel()) {
            boolean result = methodReturnClassMetaModel.isTheSameMetaModel(MAP_STRING_OBJECT_MODEL);
            log.debug("methodReturnClassMetaModel is Map<String, Object>, result: {}", result);
            return result;
        } else if (expectedReturnType.isSimpleType()) {
            boolean result = methodReturnClassMetaModel.hasRealClass()
                && methodReturnClassMetaModel.getRealClass().equals(expectedReturnType.getRealClass());
            log.debug("methodReturnClassMetaModel is simple type and class the same like expectedReturnType, result: {}", result);
            return result;
        } else if (expectedReturnType.hasGenericTypes()) {
            boolean result = methodReturnClassMetaModel.hasGenericTypes()
                && expectedReturnType.getGenericTypes().size() == expectedReturnType.getGenericTypes().size();

            log.debug("methodReturnClassMetaModel has generic types and the same size like expectedReturnType, result: {}", result);

            AtomicBoolean matchAll = new AtomicBoolean(true);
            if (result) {
                var methodGenericTypes = methodReturnClassMetaModel.getGenericTypes();
                var expectedGenericTypes = expectedReturnType.getGenericTypes();
                elements(methodGenericTypes).forEachWithIndex((index, methodArgumentType) -> {
                        matchAll.set(matchAll.get() && verifyThatReturnMethodTypeIsAsExpected(methodArgumentType, (expectedGenericTypes.get(index))));
                    }
                );
            }
            log.debug("generic types of expectedReturnType and methodReturnClassMetaModel are the same, result: {}", matchAll);

            return result && matchAll.get();
        }
        boolean result = methodReturnClassMetaModel.isSubTypeOf(expectedReturnType);
        log.debug("methodReturnClassMetaModel is sub type of expectedReturnType, result: {}", result);
        return result;
    }

    @Value
    static class InnerError {

        String propertyName;
        String message;

        static InnerError newError(String propertyName, String message) {
            return new InnerError(propertyName, message);
        }
    }

    private ClassMetaModel getClassMetaModelByDto(ClassMetaModelDto classMetaModelDto) {
        if (classMetaModelDto == null) {
            return null;
        }
        return classMetaModelMapper.toModelFromDto(classMetaModelDto);
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
            if (serviceBeanAndMethod != null && checkThatClassExists(serviceBeanAndMethod.getClassName())) {
                return ClassUtils.loadRealClass(serviceBeanAndMethod.getClassName());
            }
        }
        return null;
    }

    private boolean isNotDefaultGenericMapper(BeanAndMethodDto beanAndMethodDto) {
        return !(beanAndMethodDto.getClassName().equals(DefaultGenericMapper.class.getCanonicalName())
            && beanAndMethodDto.getMethodName().equals("mapToTarget"));
    }
}
