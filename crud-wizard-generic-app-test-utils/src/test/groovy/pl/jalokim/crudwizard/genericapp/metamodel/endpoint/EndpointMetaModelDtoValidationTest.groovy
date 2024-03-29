package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.objectToRawJson
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPersonMetaModel
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoForClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createDocumentClassMetaDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEmptyClassMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithClassName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.isIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.sampleEntryMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextSamples.createMetaModelContextWithOneEndpointInNodes
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getTemporaryMetaModelContext
import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.isTemporaryContextExists
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples.createDataStorageMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidGetListOfPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPutEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDtoSamples.sampleJoinerDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDtoSamples.createValidServiceMetaModelDtoAsScript
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.fieldShouldWhenOtherMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.invalidMinMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.notNullMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.whenFieldIsInStateThenOthersShould
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.core.exception.handler.DummyService
import pl.jalokim.crudwizard.core.validation.javax.ClassExists
import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier
import pl.jalokim.crudwizard.genericapp.mapper.instance.SomeTestMapper
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMetaModelValidationTestSpec
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumEntryMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.CannotUpdateFullDefinitionForRealClass
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.DataStorageResultsJoinCorrectness
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.PathParamsAndUrlVariablesTheSame
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.samples.NestedObject
import pl.jalokim.crudwizard.genericapp.metamodel.samples.ObjectForMergeTranslations
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeDtoWithFields
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeEnumTranslations
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeRealClass
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModel
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.NestedObject2L
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.NestedObject3L
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeDtoWithNestedFields
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService
import pl.jalokim.crudwizard.genericapp.service.GenericServiceArgument
import pl.jalokim.crudwizard.genericapp.translation.LanguagesContext
import spock.lang.Unroll

class EndpointMetaModelDtoValidationTest extends BaseMetaModelValidationTestSpec {

    private static final DS_CONNECTOR_ID = 1L
    private static final CLASS_METAMODEL = 2L

    def setup() {
        dataStorageConnectorMetaModelRepository.findExactlyOneById(DS_CONNECTOR_ID) >> DataStorageConnectorMetaModelEntity.builder()
            .nameOfQuery("some-query-name2")
            .classMetaModelInDataStorage(ClassMetaModelEntity.builder().id(CLASS_METAMODEL).build())
            .build()
        applicationContextMapping.put(SomeTestMapper, new SomeTestMapper())
    }

    @Unroll
    def "should return expected messages for: #caseName"() {
        given:
        MetaModelContext metaModelContext = createMetaModelContextWithOneEndpointInNodes()
        ModelsCache<ClassMetaModel> classMetaModels = new ModelsCache<>()
        classMetaModels.put(CLASS_METAMODEL, createPersonMetaModel())
        metaModelContext.setClassMetaModels(classMetaModels)
        metaModelContext.setDefaultServiceMetaModel(createDefaultService())
        metaModelContext.setTranslationsContext(new LanguagesContext(["en_US": "English"]))

        metaModelContextService.getMetaModelContext() >> {
            if (isTemporaryContextExists()) {
                return getTemporaryMetaModelContext()
            }
            metaModelContext
        }
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
                        .translationFieldName(sampleTranslationDto())
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
                .translationName(sampleTranslationDto())
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
                        .translationName(sampleTranslationDto())
                        .fields([
                            createValidFieldMetaModelDto("uuid", String, [], [isIdFieldType()]),
                            createValidFieldMetaModelDto("name", String),
                            createValidFieldMetaModelDto("surname", String),
                            createValidFieldMetaModelDto("document", ClassMetaModelDto.builder()
                                .name("document")
                                .translationName(sampleTranslationDto())
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
                .translationName(sampleTranslationDto())
                .name("personDto")
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("id", String, [], [isIdFieldType()]),
                    createValidFieldMetaModelDto("userLogin", String),
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
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidPathsByDot")
                                        .sourceAssignExpression("name.test.nextField")
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidOtherVariable")
                                        .sourceAssignExpression('$otherVariableName.field')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidSpringExpression")
                                        .sourceAssignExpression('@someBean.mapString()')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidMapperName")
                                        .sourceAssignExpression('@personEventMapper($rootSourceObject)')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidCastExpression")
                                        .sourceAssignExpression('((c_com.pl.NotExistsClass)$mappingContext.personId)')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidRawException")
                                        .sourceAssignExpression('j())')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("otherMapperExists")
                                        .sourceAssignExpression('@someRawDtoMapper($rootSourceObject)')
                                        .build(),
                                    PropertiesOverriddenMappingDto.builder()
                                        .targetAssignPath("invalidInnerMethodName")
                                        .sourceAssignExpression('#innerMethodNameNotExist($rootSourceObject)')
                                        .build(),
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
                            createValidFieldMetaModelDto("invalidPathsByDot", String),
                            createValidFieldMetaModelDto("invalidOtherVariable", String),
                            createValidFieldMetaModelDto("invalidSpringExpression", String),
                            createValidFieldMetaModelDto("invalidMapperName", String),
                            createValidFieldMetaModelDto("invalidCastExpression", String),
                            createValidFieldMetaModelDto("invalidRawException", String),
                            createValidFieldMetaModelDto("otherMapperExists", String),
                            createValidFieldMetaModelDto("invalidInnerMethodName", String),
                            createValidFieldMetaModelDto("document", ClassMetaModelDto.builder()
                                .name("document")
                                .translationName(sampleTranslationDto())
                                .isGenericEnumType(false)
                                .fields([
                                    createValidFieldMetaModelDto("serialNumber", String),
                                    createValidFieldMetaModelDto("validTo", LocalDate)
                                ]).build())
                        ])
                        .build())
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .dataStorageMetaModel(createDataStorageMetaModelDtoWithId(1))
                    .mapperMetaModelForPersist(MapperMetaModelDto.builder()
                        .mapperName("someRawDtoMapper")
                        .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                        .mapperBeanAndMethod(BeanAndMethodDto.builder()
                            .className(SomeTestMapper.canonicalName)
                            .methodName("mapSomeRawDto")
                            .build())
                        .build())
                    .classMetaModelInDataStorage(createClassMetaModelDtoForClass(SomeRawDto))
                    .build()

            ])
            .build()                          | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[1].targetAssignPath",
                translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "document",
                        "fieldName"      : "createdBy",
                        "currentNodeType": "document"
                    ]
                )),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[0].targetAssignPath",
                translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "",
                        "fieldName"      : "documents",
                        "currentNodeType": "personEntity"
                    ]
                )),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration" +
                ".rootConfiguration.propertyOverriddenMapping[3].targetAssignPath",
                translatePlaceholder("ClassMetaModelTypeExtractor.invalid.path",
                    [
                        "currentPath"    : "",
                        "fieldName"      : "named",
                        "currentNodeType": "personEntity"
                    ]
                )),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[4].sourceAssignExpression",
                parseExpressionMessage(11, translatePlaceholder("cannot.find.field.name", "test", String.canonicalName))),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[5].sourceAssignExpression",
                parseExpressionMessage(24, translatePlaceholder("invalid.other.variable.name", "otherVariableName"))),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[6].sourceAssignExpression",
                parseExpressionMessage(11, translatePlaceholder("cannot.find.bean.name", "someBean"))),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[7].sourceAssignExpression",
                parseExpressionMessage(20, translatePlaceholder("MappersModelsCache.not.found.mapper", "personEventMapper"))),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[8].sourceAssignExpression",
                parseExpressionMessage(27, translatePlaceholder("mapper.parser.class.not.found", "com.pl.NotExistsClass"))),

            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[9].sourceAssignExpression",
                parseExpressionMessage(4, translatePlaceholder("RawJavaCodeSourceExpressionParser.invalid.expression"))),

            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.mapperGenerateConfiguration.rootConfiguration" +
                ".propertyOverriddenMapping[11].sourceAssignExpression",
                parseExpressionMessage(43, translatePlaceholder("cannot.find.method.with.arguments",
                    [
                        "methodName"  : "innerMethodNameNotExist",
                        "classesTypes": "personDto",
                        "givenClass"  : translatePlaceholder("current.mapper.name")
                    ]

                ))),
        ]                                                      | "invalid payload with invalid target fields in mappings"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(
                createClassMetaModelDtoFromClass(ObjectForMergeTranslations)
                    .toBuilder()
                    .fields([
                        createValidFieldMetaModelDto("name", String, [], [
                            AdditionalPropertyDto.builder()
                                .name("some-property")
                                .valueRealClassName(String.canonicalName)
                                .rawJson(objectToRawJson("some-value"))
                                .build()
                        ]),
                        createValidFieldMetaModelDto("someType", SomeEnumTranslations)
                    ])
                    .build()
            )
            .build()                          | [
            errorEntry("payloadMetamodel.fields[1].fieldType.enumMetaModel",
                translatePlaceholder("ForRealClassFieldsCanBeMerged.expected.enum.translations")),
            errorEntry("payloadMetamodel.fields", "There should be a field named: id"),
            errorEntry("payloadMetamodel.fields", "There should be a field named: someObject")
        ]                                                      | "cannot find enum metamodel for real enum during check translations"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(
                createClassMetaModelDtoFromClass(ObjectForMergeTranslations)
                    .toBuilder()
                    .fields([
                        createValidFieldMetaModelDto("name", String, [], [
                            AdditionalPropertyDto.builder()
                                .name("some-property")
                                .valueRealClassName(String.canonicalName)
                                .rawJson(objectToRawJson("some-value"))
                                .build()
                        ]),
                        createValidFieldMetaModelDto("someType", SomeEnumTranslations).toBuilder()
                            .fieldType(createClassMetaModelDtoFromClass(SomeEnumTranslations).toBuilder()
                                .enumMetaModel(
                                    EnumMetaModelDto.builder()
                                        .enums([
                                            sampleEntryMetaModel("SIMPLE"),
                                            sampleEntryMetaModel("TEST_ENUM"),
                                            EnumEntryMetaModelDto.builder()
                                                .build()
                                        ])
                                        .build()
                                )
                                .build())
                            .build()
                    ])
                    .build()
            )
            .build()                          | [
            errorEntry("payloadMetamodel.fields", "There should be a field named: someObject"),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums[2]', 'Unknown enum entry'),
            errorEntry('payloadMetamodel.fields', 'There should be a field named: id'),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums[1]', 'Unknown enum entry'),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums', 'Not given translation for enum: FULL'),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums', 'Not given translation for enum: MEDIUM'),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums[2].name', 'must not be blank'),
            errorEntry('payloadMetamodel.fields[1].fieldType.enumMetaModel.enums[2].translation', 'must not be null'),
        ]                                                      | "invalid translations for enums"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(
                createClassMetaModelDtoFromClass(ObjectForMergeTranslations)
                    .toBuilder()
                    .fields([
                        createValidFieldMetaModelDto("id", Long),
                        createValidFieldMetaModelDto("someObject", createClassMetaModelDtoFromClass(NestedObject).toBuilder()
                            .fields([
                                createValidFieldMetaModelDto("objectName", String)
                            ])
                            .build()),
                        createValidFieldMetaModelDto("name", String, [], [
                            AdditionalPropertyDto.builder()
                                .name("some-property")
                                .valueRealClassName(String.canonicalName)
                                .rawJson(objectToRawJson("some-value"))
                                .build()
                        ]),
                        createValidFieldMetaModelDto("someType", SomeEnumTranslations).toBuilder()
                            .fieldType(createClassMetaModelDtoFromClass(SomeEnumTranslations).toBuilder()
                                .enumMetaModel(
                                    EnumMetaModelDto.builder()
                                        .enums([
                                            sampleEntryMetaModel("SIMPLE"),
                                            sampleEntryMetaModel("FULL"),
                                            sampleEntryMetaModel("MEDIUM")
                                        ])
                                        .build()
                                )
                                .build())
                            .build()
                    ])
                    .build()
            )
            .build()                          | []             | "valid translations for enums"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(createClassMetaModelDtoFromClass(SomeRealClass).toBuilder()
                .fields([
                    createValidFieldMetaModelDto("id", Long),
                    createValidFieldMetaModelDto("name", String),
                ])
                .build())
            .build()                          | [
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "someObject"))
        ]                                                      | "lack of 1 translated fields for class metamodel with classname"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(createClassMetaModelDtoFromClass(SomeRealClass).toBuilder()
                .fields([])
                .translationName(null).build())
            .build()                          | [
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "id")),
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "name")),
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "someObject")),
            errorEntry("payloadMetamodel.translationName", notNullMessage())
        ]                                                      | "lack of 3 fields for class metamodel with classname"
        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(createClassMetaModelDtoFromClass(SomeDtoWithNestedFields).toBuilder()
                .fields([
                    createValidFieldMetaModelDto("uuid", String),
                    createValidFieldMetaModelDto("refId", Long),
                    createValidFieldMetaModelDto("referenceNumber", String),
                    createValidFieldMetaModelDto("otherField",
                        createClassMetaModelDtoFromClass(SomeRawDto).toBuilder()
                            .extendsFromModels([createClassMetaModelDtoFromClass(Long)])
                            .fields([
                                createValidFieldMetaModelDto("id", Long),
                                createValidFieldMetaModelDto("surname", String),
                            ])
                            .build()),
                    createValidFieldMetaModelDto("level2", createClassMetaModelDtoFromClass(NestedObject2L).toBuilder()
                        .fields([
                            createValidFieldMetaModelDto("id", Long),
                            createValidFieldMetaModelDto("level3", createClassMetaModelDtoFromClass(NestedObject3L).toBuilder()
                                .fields([
                                    createValidFieldMetaModelDto("realUuid", UUID),
                                ])
                                .build()),
                        ])
                        .build()),
                    createValidFieldMetaModelDto("level22",
                        createClassMetaModelDtoFromClass(NestedObject2L).toBuilder()
                            .extendsFromModels([createClassMetaModelDtoFromClass(Long)])
                            .fields([
                                createValidFieldMetaModelDto("id", String),
                                createValidFieldMetaModelDto("level3", NestedObject3L),
                            ])
                            .build()),
                ])
                .build())
            .build()                          | [
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "createdDate")),
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "hashValue")),
            errorEntry("payloadMetamodel.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "superId")),
            errorEntry("payloadMetamodel.fields[3].fieldType.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "name")),
            errorEntry("payloadMetamodel.fields[5].fieldType.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "serialNumber")),
            errorEntry("payloadMetamodel.fields[4].fieldType.fields", getMessage("OnlyExpectedFieldsForRealClass.expected.field.not.found", "serialNumber")),
            errorEntry("payloadMetamodel.fields[3].fieldType.extendsFromModels", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("payloadMetamodel.fields[5].fieldType.extendsFromModels", whenFieldIsInStateThenOthersShould(
                "classMetaModelDtoType", EQUAL_TO_ANY, ["DEFINITION"],
                fieldShouldWhenOtherMessage(NULL, [], "className", NOT_NULL, []))),
            errorEntry("payloadMetamodel.fields[4].fieldType.fields[0].fieldType",
                getMessage("ForRealClassFieldsCanBeMerged.invalid.field.type", String.canonicalName))
        ]                                                      | "problem with nested fields names etc"

        createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(createClassMetaModelDtoFromClass(SomeDtoWithFields))
            .build()                          | [
                errorEntry("payloadMetamodel", messageForValidator(CannotUpdateFullDefinitionForRealClass))
            ]             | "cannot update real class definition"
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
        metaModelContext.setTranslationsContext(new LanguagesContext(["en_US": "American English"]))
        metaModelContextService.getMetaModelContext() >> metaModelContext
        metaModelContextService.loadNewMetaModelContext() >> metaModelContext
        temporaryContextLoader.loadTemporaryContextFor(endpointMetaModelDto)
        beforeEndpointValidatorUpdater.beforeValidation(endpointMetaModelDto)

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

    private static String parseExpressionMessage(int columnNumber, String message) {
        translatePlaceholder("MapperContextEntryError.column") + ":" + columnNumber + ", " + message
    }
}
