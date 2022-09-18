package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.createObjectMapper
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPersonMetaModel
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createDocumentClassMetaDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.isIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidGetListOfPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPutEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDtoSamples.sampleJoinerDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidMinMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.whenFieldIsInStateThenOthersShould
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter.createValidatorWithConverter
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import javax.validation.ValidatorFactory
import org.mapstruct.factory.Mappers
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpMethod
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder
import pl.jalokim.crudwizard.core.validation.javax.ClassExists
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorageFactory
import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.TemporaryContextLoader
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelTypeExtractor
import pl.jalokim.crudwizard.genericapp.metamodel.context.EndpointMetaModelContextNodeUtils
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageInstances
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.DataStorageResultsJoinCorrectness
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.PathParamsAndUrlVariablesTheSame
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel
import pl.jalokim.crudwizard.genericapp.provider.GenericBeansProvider
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator
import pl.jalokim.crudwizard.genericapp.service.invoker.MethodSignatureMetaModelResolver
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class EndpointMetaModelDtoValidationTest extends UnitTestSpec {

    private static final DS_CONNECTOR_ID = 1L
    private static final CLASS_METAMODEL = 2L

    private MetaModelContextService metaModelContextService = Mock()
    private ApplicationContext applicationContext = Mock()
    private JdbcTemplate jdbcTemplate = Mock()
    private ValidatorFactory validatorFactory = Mock()
    private DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository = Mock()
    private DataStorageInstances dataStorageInstances = Mock()
    private ClassMetaModelMapper classMetaModelMapper = Mappers.getMapper(ClassMetaModelMapper)
    private MapperMetaModelMapper mapperMetaModelMapper = Mappers.getMapper(MapperMetaModelMapper)
    private ClassMetaModelTypeExtractor classMetaModelTypeExtractor = new ClassMetaModelTypeExtractor(classMetaModelMapper)
    private jsonObjectMapper = new JsonObjectMapper(createObjectMapper())
    private endpointMetaModelContextNodeUtils = new EndpointMetaModelContextNodeUtils(jsonObjectMapper, metaModelContextService)
    private MethodSignatureMetaModelResolver methodSignatureMetaModelResolver = new MethodSignatureMetaModelResolver(jsonObjectMapper)
    private validatorWithConverter = createValidatorWithConverter(endpointMetaModelContextNodeUtils, applicationContext,
        dataStorageConnectorMetaModelRepository, classMetaModelTypeExtractor, metaModelContextService,
        jdbcTemplate, dataStorageInstances, methodSignatureMetaModelResolver, classMetaModelMapper)
    private BeforeEndpointValidatorUpdater beforeEndpointValidatorUpdater = new BeforeEndpointValidatorUpdater()
    private TemporaryContextLoader temporaryContextLoader = new TemporaryContextLoader(validatorFactory,
        metaModelContextService, classMetaModelMapper, mapperMetaModelMapper
    )

    def setup() {
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", Mappers.getMapper(FieldMetaModelMapper))
        setValueForField(classMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))

        GenericBeansProvider genericBeanProvider = Mock()
        InstanceLoader instanceLoader = Mock()

        setValueForField(mapperMetaModelMapper, "genericBeanProvider", genericBeanProvider)
        setValueForField(mapperMetaModelMapper, "instanceLoader", instanceLoader)
        setValueForField(mapperMetaModelMapper, "beanMethodMetaModelCreator", new BeanMethodMetaModelCreator(
            new MethodSignatureMetaModelResolver(jsonObjectMapper)))
        setValueForField(mapperMetaModelMapper, "classMetaModelMapper", classMetaModelMapper)
        setValueForField(mapperMetaModelMapper, "rawAdditionalPropertyMapper", Mappers.getMapper(RawAdditionalPropertyMapper))

        RequestMappingHandlerMapping abstractHandlerMethodMapping = Mock()
        applicationContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class) >> abstractHandlerMethodMapping

        abstractHandlerMethodMapping.getHandlerMethods() >> [:]
        jdbcTemplate.queryForObject(_ as String, _ as Class<?>) >> 0
        dataStorageInstances.getDataStorageFactoryForClass(_) >> Mock(DataStorageFactory)

        dataStorageConnectorMetaModelRepository.findExactlyOneById(DS_CONNECTOR_ID) >> DataStorageConnectorMetaModelEntity.builder()
            .nameOfQuery("some-query-name2")
            .classMetaModelInDataStorage(ClassMetaModelEntity.builder().id(CLASS_METAMODEL).build())
            .build()

        validatorFactory.getValidator() >> validatorWithConverter.getValidator()
    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }

    @Unroll
    def "should return expected messages for: #caseName"() {
        given:
        MetaModelContext metaModelContext = createMetaModelContextWithOneEndpointInNodes()
        ModelsCache<ClassMetaModel> classMetaModels = new ModelsCache<>()
        classMetaModels.put(CLASS_METAMODEL, createPersonMetaModel())
        metaModelContext.setClassMetaModels(classMetaModels)
        metaModelContext.setDefaultServiceMetaModel(createDefaultService())

        metaModelContextService.getMetaModelContext() >> metaModelContext
        metaModelContextService.loadNewMetaModelContext() >> metaModelContext

        beforeEndpointValidatorUpdater.beforeValidation(endpointMetaModelDto)
        temporaryContextLoader.loadTemporaryContextFor(endpointMetaModelDto)

        applicationContext.getBean("dummyService") >> new DummyService()

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(endpointMetaModelDto)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        endpointMetaModelDto                  | expectedErrors | caseName
        createValidPostEndpointMetaModelDto() | []             | "valid post endpoint"

        createValidPostEndpointMetaModelDto().toBuilder()
            .serviceMetaModel(createValidServiceMetaModelDto())
            .build()                          | []             | "valid post endpoint and custom service"

        emptyEndpointMetaModelDto()           | [
            errorEntry("apiTag", notNullMessage()),
            errorEntry("baseUrl", notNullMessage()),
            errorEntry("httpMethod", notNullMessage()),
            errorEntry("operationName", notNullMessage())
        ]                                                      | "invalid empty endpoint dto"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(null)
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("responseMetaModel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "POST"]
            ))
        ]                                                      | "lack of payloadMetamodel responseMetaModel for post"

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.PUT)
            .responseMetaModel(null)
            .payloadMetamodel(null)
            .pathParams(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("pathParams", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["PUT", "PATCH"]
            ))
        ]                                                      | "invalid payloadMetamodel, pathParams for PUT"

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.PATCH)
            .responseMetaModel(null)
            .payloadMetamodel(null)
            .pathParams(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["POST", "PUT", "PATCH"]
            )),
            errorEntry("pathParams", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["PUT", "PATCH"]
            ))
        ]                                                      | "invalid payloadMetamodel, pathParams for PATCH"

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.GET)
            .payloadMetamodel(createEmptyClassMetaModelDto())
            .queryArguments(createEmptyClassMetaModelDto())
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "DELETE"]
            )),
            errorEntry("responseMetaModel", fieldShouldWhenOtherMessage(
                NOT_NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "POST"]
            )),
            errorEntry("payloadMetamodel.name",
                whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, [])))
        ]                                                      | "invalid GET endpoint"

        createValidPostEndpointMetaModelDto().toBuilder()
            .httpMethod(HttpMethod.DELETE)
            .responseMetaModel(null)
            .build()                          | [
            errorEntry("payloadMetamodel", fieldShouldWhenOtherMessage(
                NULL, [], "httpMethod", EQUAL_TO_ANY, ["GET", "DELETE"])
            ),
            errorEntry("dataStorageConnectors", fieldShouldWhenOtherMessage(
                NOT_EMPTY, [], "httpMethod", EQUAL_TO_ANY, ["DELETE"]))
        ]                                                      | "invalid DELETE endpoint"

        createValidPostEndpointMetaModelDto().toBuilder()
            .serviceMetaModel(
                createValidServiceMetaModelDtoAsScript().toBuilder()
                    .serviceBeanAndMethod(BeanAndMethodDto.builder()
                        .build())
                    .build()
            )
            .build()                          | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod", fieldShouldWhenOtherMessage(
                NULL, [], "serviceScript", NOT_NULL, []
            )),
            errorEntry("serviceMetaModel.serviceScript", fieldShouldWhenOtherMessage(
                NULL, [], "serviceBeanAndMethod", NOT_NULL, []
            )),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName", notNullMessage()),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.className", notNullMessage())
        ]                                                      | "invalid serviceMetaModel fields for some POST"

        createValidPostEndpointMetaModelDto().toBuilder()
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createEmptyClassMetaModelDto())
                .successHttpCode(10)
                .build()
            )
            .build()                          | [
            errorEntry("responseMetaModel.classMetaModel.name",
                whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))),
            errorEntry("responseMetaModel.successHttpCode", invalidMinMessage(100))
        ]                                                      | "invalid responseMetaModel fields for some POST"

        createValidPostEndpointMetaModelDto().toBuilder()
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createValidClassMetaModelDtoWithClassName())
                .build()
            ).build()                         | []             | "valid post with custom response model"

        createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(DataStorageMetaModelDto.builder().build())
                    .mapperMetaModelForPersist(MapperMetaModelDto.builder()
                        .mapperBeanAndMethod(BeanAndMethodDto.builder()
                            .beanName(randomText())
                            .build())
                        .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                        .build())
                    .classMetaModelInDataStorage(createEmptyClassMetaModelDto())
                    .build()
            ])
            .build()                          | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperBeanAndMethod.methodName", notNullMessage()),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperBeanAndMethod.className", notNullMessage()),
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.name",
                fieldShouldWhenOtherMessage(NOT_NULL, [], "id", NULL, [])),
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.className",
                fieldShouldWhenOtherMessage(NOT_NULL, [], "id", NULL, [])),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage",
                createMessagePlaceholder("ClassMetaModel.id.field.not.found", "").translateMessage()),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage.name",
                whenFieldIsInStateThenOthersShould("id", NULL, fieldShouldWhenOtherMessage(NOT_NULL, [], "className", NULL, []))),
        ]                                                      | "invalid dataStorageConnectors fields for some POST"

        createValidPutEndpointMetaModelDto()  | []             | "valid PUT endpoint"

        createValidPutEndpointMetaModelDto().toBuilder()
            .baseUrl("base-path/{basePath}/next-url")
            .build()                          | [
            errorEntry("", messageForValidator(PathParamsAndUrlVariablesTheSame, [
                baseUrl   : "base-path/{basePath}/next-url",
                fieldName : wrapAsExternalPlaceholder("pathParams"),
                fieldNames: "basePath, nextId"
            ]))
        ]                                                      | "invalid PathParamsAndUrlVariablesTheSame for some PUT"

        createValidPutEndpointMetaModelDto().toBuilder()
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("basePath", Double),
                    FieldMetaModelDto.builder()
                        .fieldName("nextId")
                        .fieldType(createValidClassMetaModelDtoWithName())
                        .build()

                ])
                .build())
            .build()                          | [
            errorEntry("", getMessage(PathParamsAndUrlVariablesTheSame, "allFieldsShouldHasClassName"))
        ]                                                      | "invalid PathParamsAndUrlVariablesTheSame for some PUT and allFieldsShouldHasClassName"

        createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("users/{userId}/orders/{orderId}")
            .build()                          | [
            errorEntry("", createMessagePlaceholder(EndpointNotExistsAlready, "crudWizardController", [
                url               : "users/{userId}/orders/{orderId}",
                httpMethod        : "POST",
                foundUrl          : "users/{usersIdVar}/orders/{ordersIdVar}",
                foundOperationName: "existOperationName",
            ]).translateMessage()),
            errorEntry("", messageForValidator(PathParamsAndUrlVariablesTheSame, [
                baseUrl   : "users/{userId}/orders/{orderId}",
                fieldName : wrapAsExternalPlaceholder("pathParams"),
                fieldNames: ""
            ]))
        ]                                                      | "expected invalid: EndpointNotExistsAlready and PathParamsAndUrlVariablesTheSame"

        createValidGetListOfPerson()          | []             | "valid GET list of person"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto(randomText(), randomText(), randomText(), randomText()),
                sampleJoinerDto(randomText(), randomText(), randomText(), randomText())
            ])
            .build()                          | [
            errorEntry("dataStorageResultsJoiners", getMessage(DataStorageResultsJoinCorrectness, "invalidJoinersNumber"))
        ]                                                      | "invalid joiners number"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto("first-db", randomText(), "first-db", randomText()).toBuilder()
                    .joinerVerifierClassName("invalidClassName")
                    .build()
            ])
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
            ])
            .build()                          | [
            errorEntry("dataStorageResultsJoiners[0].joinerVerifierClassName",
                messageForValidator(ClassExists, "expectedOfType", ObjectsJoinerVerifier.canonicalName)),
            errorEntry("dataStorageConnectors[1]", getMessage(DataStorageResultsJoinCorrectness, "nonUniqueDsOrQueryName"))
        ]                                                      | "invalid joiner class type, names of queryOrDsName are not unique in dsConnectors"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto(randomText(), randomText(), randomText(), randomText()),
                sampleJoinerDto("some-query-name", randomText(), randomText(), randomText()),
            ])
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name2"),
            ])
            .build()                          | [
            errorEntry("dataStorageResultsJoiners[0].leftNameOfQueryResult", getMessage(DataStorageResultsJoinCorrectness, "notFound")),
            errorEntry("dataStorageResultsJoiners[0].rightNameOfQueryResult", getMessage(DataStorageResultsJoinCorrectness, "notFound")),
            errorEntry("dataStorageResultsJoiners[1].rightNameOfQueryResult", getMessage(DataStorageResultsJoinCorrectness, "notFound"))
        ]                                                      | "names of queryOrDsName are not found in dsConnectors left and right"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto("first-db", randomText(), "some-query-name", randomText()),
                sampleJoinerDto("some-query-name", randomText(), "some-query-name2", randomText()),
                sampleJoinerDto("first-db", randomText(), "some-query-name2", randomText()),
                sampleJoinerDto("some-query-name3", randomText(), "some-query-name5", randomText()),
                sampleJoinerDto("some-query-name4", randomText(), "some-query-name5", randomText()),
            ])
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name2"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name3"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name4"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name5"),
            ])
            .build()                          | [
            errorEntry("dataStorageResultsJoiners", getMessage(DataStorageResultsJoinCorrectness.class, "notOneGroupResults",
                "[first-db, some-query-name, some-query-name2], [some-query-name3, some-query-name5, some-query-name4]")),
            errorEntry("dataStorageResultsJoiners[2]", getMessage(DataStorageResultsJoinCorrectness, "existsInTheSameGroupAlready",
                "first-db, some-query-name, some-query-name2"))
        ]                                                      | "invalid joiners already in the same group and not one group results"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto("first-db", "id", "some-query-name", "someNumber"),
                sampleJoinerDto("some-query-name", "lastLogin", "some-query-name2", "fatherData.someDateTimeField"),
                sampleJoinerDto("some-query-name4", "value", "some-query-name5", "surname"),
                sampleJoinerDto("some-query-name3", "birthDay", "some-query-name5", "birthDate"),
                sampleJoinerDto("some-query-name4", "validTo", "some-query-name2", "passportData.validTo"),
            ])
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
                createSampleDataStorageConnectorDto(exampleClassMetaModelDtoWithExtension(), "some-query-name"),
                DataStorageConnectorMetaModelDto.builder().id(DS_CONNECTOR_ID).build(),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoFromClass(ExtendedSamplePersonDto), "some-query-name3"),
                createSampleDataStorageConnectorDto(createDocumentClassMetaDto(), "some-query-name4"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name5"),
            ])
            .build()                          | []             | "valid GET with joiners"

        createValidGetListOfPerson().toBuilder()
            .dataStorageResultsJoiners([
                sampleJoinerDto("first-db", "id.nextField", "some-query-name", "someNumber"),
                sampleJoinerDto("some-query-name", "lastLogin", "some-query-name2", "fatherData.someDateTimeField"),
                sampleJoinerDto("some-query-name4", "type", "some-query-name5", "surname"),
                sampleJoinerDto("some-query-name3", "birthDay", "some-query-name5", "?birthDay"),
                sampleJoinerDto("some-query-name4", "validTo", "some-query-name2", "passportData.documentNumber2.?nextNode"),
            ])
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel()),
                createSampleDataStorageConnectorDto(exampleClassMetaModelDtoWithExtension(), "some-query-name"),
                DataStorageConnectorMetaModelDto.builder().id(DS_CONNECTOR_ID).build(),
                createSampleDataStorageConnectorDto(createClassMetaModelDtoFromClass(ExtendedSamplePersonDto), "some-query-name3"),
                createSampleDataStorageConnectorDto(createDocumentClassMetaDto(), "some-query-name4"),
                createSampleDataStorageConnectorDto(simplePersonClassMetaModel(), "some-query-name5"),
            ])
            .build()                          | [
            errorEntry("dataStorageResultsJoiners[0].leftPath", getMessage("ClassMetaModelTypeExtractor.not.expected.any.field",
                Map.of("currentPath", "id",
                    "currentNodeType", Long.canonicalName))),
            errorEntry("dataStorageResultsJoiners[4].rightPath", getMessage("ClassMetaModelTypeExtractor.invalid.path",
                [currentPath    : "passportData",
                 fieldName      : "documentNumber2",
                 currentNodeType: "document"])),
            errorEntry("dataStorageResultsJoiners[2]", getMessage(DataStorageResultsJoinCorrectness.class, "notTheSameTypesForJoin",
                [leftType : Byte.canonicalName,
                 rightType: String.canonicalName]))
        ]                                                      | "invalid left path and invalid right path, and not the same types for join"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personDto")
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("id", String, [], [isIdFieldType()]),
                    createValidFieldMetaModelDto("name", String),
                    createValidFieldMetaModelDto("surname", String),
                    createValidFieldMetaModelDto("documentSerialNumber", String),
                    createValidFieldMetaModelDto("documentValidTo", LocalDate),
                ])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(1))
                    .mapperMetaModelForPersist(MapperMetaModelDto.builder()
                        .mapperName("personDtoToEntityMapper")
                        .mapperType(MapperType.GENERATED)
                        .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                            .rootConfiguration(MapperConfigurationDto.builder()
                                .name("personDtoToEntityMapper")
                                .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                .propertyOverriddenMapping([
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("document.serialNumber")
                                        .sourceAssignExpression("documentSerialNumber")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("document.validTo")
                                        .sourceAssignExpression("documentValidTo")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("uuid")
                                        .sourceAssignExpression("id")
                                        .build()
                                ])
                                .build())
                            .build())
                        .build())
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("uuid", String, [], [isIdFieldType()]),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("surname", String),
                            createValidFieldMetaModelDto("document", ClassMetaModelDto.builder()
                                .name("document")
                                .isGenericEnumType(false)
                                .fields([
                                    createValidFieldMetaModelDto("serialNumber", String),
                                    createValidFieldMetaModelDto("validTo", LocalDate)
                                ]).build())
                        ])
                        .build())
                    .build()
            ])
            .build()                          | []             | "valid payload with few mappers"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personDto")
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("id", String, [], [isIdFieldType()]),
                    createValidFieldMetaModelDto("name", String),
                    createValidFieldMetaModelDto("surname", String),
                    createValidFieldMetaModelDto("documentSerialNumber", String),
                    createValidFieldMetaModelDto("documentValidTo", LocalDate),
                ])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(1))
                    .mapperMetaModelForPersist(MapperMetaModelDto.builder()
                        .mapperName("personDtoToEntityMapper")
                        .mapperType(MapperType.GENERATED)
                        .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                            .rootConfiguration(MapperConfigurationDto.builder()
                                .name("personDtoToEntityMapper")
                                .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                .propertyOverriddenMapping([
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("documents.serialNumber")
                                        .sourceAssignExpression("documentSerialNumber")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("document.createdBy")
                                        .sourceAssignExpression("documentValidTo")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("uuid")
                                        .sourceAssignExpression("id")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("named")
                                        .sourceAssignExpression("name")
                                        .build()
                                ])
                                .build())
                            .build())
                        .build())
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .isGenericEnumType(false)
                        .fields([
                            createValidFieldMetaModelDto("uuid", String, [], [isIdFieldType()]),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("surname", String),
                            createValidFieldMetaModelDto("document", ClassMetaModelDto.builder()
                                .name("document")
                                .isGenericEnumType(false)
                                .fields([
                                    createValidFieldMetaModelDto("serialNumber", String),
                                    createValidFieldMetaModelDto("validTo", LocalDate)
                                ]).build())
                        ])
                        .build())
                    .build()
            ])
            .build()                          | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[1].targetAssignPath",
                MessagePlaceholder.translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "document",
                        "fieldName"      : "createdBy",
                        "currentNodeType": "document"
                    ]
                )),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[0].targetAssignPath",
                MessagePlaceholder.translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "",
                        "fieldName"      : "documents",
                        "currentNodeType": "personEntity"
                    ]
                )),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[3].targetAssignPath",
                MessagePlaceholder.translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "",
                        "fieldName"      : "named",
                        "currentNodeType": "personEntity"
                    ]
                ))
        ]                                                      | "invalid payload with invalid target fields in mappings"
    }

    private ServiceMetaModel createDefaultService() {

        def method = getMethod(DefaultGenericService, "saveOrReadFromDataStorages", GenericServiceArgument)
        def defaultGenericServiceInstance = new DefaultGenericService(null, null, null, null, null)

        ServiceMetaModel.builder()
            .serviceInstance(defaultGenericServiceInstance)
            .serviceBeanAndMethod(BeanAndMethodMetaModel.builder()
                .className(DefaultGenericService.canonicalName)
                .beanName("defaultGenericService")
                .methodName("saveOrReadFromDataStorages")
                .originalMethod(method)
                .methodSignatureMetaModel(methodSignatureMetaModelResolver.resolveMethodSignature(method, DefaultGenericService))
                .build())
            .build()
    }

    @Unroll
    def "should return expected messages for update context of EndpointMetaModelDto"() {
        given:
        MetaModelContext metaModelContext = new MetaModelContext()
        metaModelContext.setDefaultServiceMetaModel(createDefaultService())
        metaModelContext.setClassMetaModels(new ModelsCache())
        metaModelContextService.getMetaModelContext() >> metaModelContext
        metaModelContextService.loadNewMetaModelContext() >> metaModelContext
        temporaryContextLoader.loadTemporaryContextFor(endpointMetaModelDto)

        when:
        def foundErrors = validatorWithConverter.validateAndReturnErrors(endpointMetaModelDto, EndpointUpdateContext)

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        endpointMetaModelDto           | expectedErrors
        createValidPostEndpointMetaModelDto()
            .toBuilder().id(1).build() | []

        emptyEndpointMetaModelDto()    | [
            errorEntry("id", notNullMessage()),
            errorEntry("apiTag", notNullMessage()),
            errorEntry("baseUrl", notNullMessage()),
            errorEntry("httpMethod", notNullMessage()),
            errorEntry("operationName", notNullMessage())
        ]
    }
}
