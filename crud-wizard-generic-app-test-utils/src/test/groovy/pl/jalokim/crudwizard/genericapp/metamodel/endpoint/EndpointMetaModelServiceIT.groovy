package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.objectToRawJson
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoForClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithGenerics
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel1
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.sampleEntryMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel.PLACEHOLDER_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators2
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService.createNewEndpointReason
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDto
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.notNullValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.classNotExistsMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Predicate
import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpMethod
import org.springframework.util.CollectionUtils
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto
import pl.jalokim.crudwizard.core.sample.Agreement
import pl.jalokim.crudwizard.core.sample.PersonEvent
import pl.jalokim.crudwizard.core.utils.InstanceLoader
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue
import pl.jalokim.crudwizard.genericapp.compiler.CompiledCodeRootPathProvider
import pl.jalokim.crudwizard.genericapp.customendpoint.SomeCustomRestController
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1
import pl.jalokim.crudwizard.genericapp.mapper.defaults.DefaultGenericMapper
import pl.jalokim.crudwizard.genericapp.mapper.instance.SomeTestMapper
import pl.jalokim.crudwizard.genericapp.metamodel.MetaModelDtoType
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ExistFullDefinitionInTempContextByName
import pl.jalokim.crudwizard.genericapp.metamodel.context.ContextRefreshStatus
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshRepository
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.EqObjectsJoiner
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.validation.VerifyThatCanCreateDataStorage
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
import pl.jalokim.crudwizard.genericapp.metamodel.samples.NestedObject
import pl.jalokim.crudwizard.genericapp.metamodel.samples.ObjectForMergeTranslations
import pl.jalokim.crudwizard.genericapp.metamodel.samples.SomeEnumTranslations
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.rest.samples.datastorage.DataStorageWithoutFactory
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.BeanAndMethodDtoTestService
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.MapGenericService
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter
import spock.lang.Unroll

class EndpointMetaModelServiceIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private EndpointMetaModelRepository endpointMetaModelRepository

    @Autowired
    private MetaModelContextRefreshRepository metaModelContextRefreshRepository

    @Autowired
    private InstanceLoader instanceLoader

    @Autowired
    private ValidatorMetaModelRepository validatorMetaModelRepository

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private ClassMetaModelRepository classMetaModelRepository

    @Autowired
    private CompiledCodeRootPathProvider codeRootPathProvider

    def "should save POST new endpoint with default mapper, service, data storage"() {
        given:
        def instancesCache = instanceLoader.notSpringBeanInstancesByClass
        instanceLoader.clearInstancesCache()
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(extendedPersonClassMetaModel1())
            .payloadMetamodelAdditionalValidators(createAdditionalValidatorsForExtendedPerson())
            .build()

        when:
        def createdId = endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.findExactlyOneById(createdId)
            verifyAll(endpointEntity) {
                verifyAll(apiTag) {
                    id != null
                    name == createEndpointMetaModelDto.apiTag.name
                }
                baseUrl == createEndpointMetaModelDto.baseUrl
                httpMethod == createEndpointMetaModelDto.httpMethod
                operationName == createEndpointMetaModelDto.operationName

                def allValidators = validatorMetaModelRepository.findAll()
                allValidators.size() == 5
                def notNullValidator = allValidators.find {
                    it.className == NotNullValidator.canonicalName
                }
                def documentValueSizeValidator = findSizeValidator(allValidators, 5, 25)
                def additionalPersonNameSizeValidator = findSizeValidator(allValidators, 2, 20)
                def additionalPersonSurnameSizeValidator = findSizeValidator(allValidators, 2, 30)
                def additionalDocumentsSizeValidator = findSizeValidator(allValidators, 1, null)

                def inputPayloadMetamodel = createEndpointMetaModelDto.payloadMetamodel
                assertClassMetaModels(payloadMetamodel, inputPayloadMetamodel)

                def foundDocumentsEntity = payloadMetamodel.fields.find {
                    it.fieldName == "documents"
                }

                inTransaction {
                    def foundDocumentEntity = foundDocumentsEntity.fieldType.genericTypes[0]

                    def foundValueFieldEntity = foundDocumentEntity.fields.find {
                        it.fieldName == "value"
                    }

                    assert foundDocumentEntity.validators == [notNullValidator]
                    assert foundValueFieldEntity.validators == [notNullValidator, documentValueSizeValidator]
                }

                assertAdditionalValidators(payloadMetamodelAdditionalValidators, "name", notNullValidator, additionalPersonNameSizeValidator)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators, "surname", notNullValidator, additionalPersonSurnameSizeValidator)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators, "documents", additionalDocumentsSizeValidator)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators, "documents[*].type", notNullValidator)

                verifyAll(responseMetaModel) {
                    verifyAll(classMetaModel) {
                        name == null
                        className == createEndpointMetaModelDto.responseMetaModel.classMetaModel.className
                    }
                    successHttpCode == createEndpointMetaModelDto.responseMetaModel.successHttpCode
                }
            }
        }
        inTransaction {
            def foundRefreshEntity = metaModelContextRefreshRepository.findAll()
                .find {
                    it.refreshReason == createNewEndpointReason(createdId)
                }
            assert foundRefreshEntity.contextRefreshStatus == ContextRefreshStatus.CORRECT
        }
        instancesCache.get(NotNullValidator) != null
        instancesCache.get(SizeValidator) != null
    }

    private boolean assertClassMetaModels(ClassMetaModelEntity classMetaModelEntity, ClassMetaModelDto classMetaModelDto) {
        verifyAll(classMetaModelEntity) {
            name == classMetaModelDto.name
            className == classMetaModelDto.className

            List<FieldMetaModelEntity> fieldsEntities = Optional.ofNullable(fields).orElse([])
            List<FieldMetaModelDto> fieldsDto = Optional.ofNullable(classMetaModelDto.fields).orElse([])

            fieldsEntities.size() == fieldsDto.size()
            if (fieldsEntities.size() > 0) {
                fieldsEntities.eachWithIndex {fieldEntity, index ->
                    verifyAll(fieldEntity) {
                        def fieldDto = fieldsDto[index]
                        fieldName == fieldDto.fieldName
                        assertClassMetaModels(fieldType, fieldDto.fieldType)
                    }
                }
            }
        }
        return true
    }

    private ValidatorMetaModelEntity findSizeValidator(List<ValidatorMetaModelEntity> allValidators, Long min, Long max) {
        allValidators.find {
            it.className == SizeValidator.canonicalName &&
                (min == null || foundValidatorPlaceholder(it.additionalProperties, "min", min)) &&
                (max == null || foundValidatorPlaceholder(it.additionalProperties, "max", max))
        }
    }

    private static boolean assertAdditionalValidators(List<AdditionalValidatorsEntity> additionalValidators,
        String fullPropertyPath, ValidatorMetaModelEntity... expectedValidators) {
        def foundAdditionalValidatorsEntry = additionalValidators.find {
            it.fullPropertyPath == fullPropertyPath
        }
        foundAdditionalValidatorsEntry.getValidators() as Set == expectedValidators as Set
    }

    private static boolean foundValidatorPlaceholder(List<AdditionalPropertyEntity> additionalProperties, String name, Long value) {
        additionalProperties.find {
            it.name == PLACEHOLDER_PREFIX + name && it.rawJson == value.toString()
        } != null
    }

    def "should throw ConstraintViolationException when bean is invalid (verify only that aspect was invoked)"() {
        given:
        def createEndpointMetaModelDto = emptyEndpointMetaModelDto()

        when:
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        ConstraintViolationException tx = thrown()
        isNotEmpty(tx.constraintViolations)
    }

    def "should not save endpoint when it override custom endpoint from spring"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("some-endpoint/{someId}/second-part/{partId}")
            .pathParams(ClassMetaModelDto.builder()
                .name(randomText())
                .fields([
                    createValidFieldMetaModelDto("someId", String),
                    createValidFieldMetaModelDto("partId", Long)
                ])
                .build())
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("", createMessagePlaceholder(EndpointNotExistsAlready, "springRestController", [
                url               : "some-endpoint/{someId}/second-part/{partId}",
                httpMethod        : "POST",
                restClassAndMethod: "$SomeCustomRestController.canonicalName#somePost(Long, String)"
            ]).translateMessage())
        ])
    }

    def "should return expected number of class metamodels when contains the same name and contents"() {
        given:
        def createPostPersonEndpoint = createValidPostExtendedUserWithValidators2()

        when:
        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)

        then:
        inTransaction {
            def classMetaModels = metaModelContextService.getMetaModelContext().getClassMetaModels().fetchAll()
            assert classMetaModels.size() == 8

            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.name == "simple-person"
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.name == "document"
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel ->
                    classMetaModel.realClass == List &&
                        classMetaModel.genericTypes.size() == 1 &&
                        classMetaModel.genericTypes.find {
                            it.name == "document"
                        } != null
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.name == "exampleEnum" && classMetaModel.isGenericMetamodelEnum()
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == Long && classMetaModel.simpleRawClass
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == Byte && classMetaModel.simpleRawClass
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == String && classMetaModel.simpleRawClass
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == LocalDate && classMetaModel.simpleRawClass
            })
        }
    }

    def "should return validation messages about unique names and cannot find data storage factory"() {
        given:
        def documentClassMetaDto = ClassMetaModelDtoSamples.createDocumentClassMetaDto()
        def sampleEndpoint = createValidPostExtendedUserWithValidators2()
        def payloadMetaModel = sampleEndpoint.getPayloadMetamodel()
        def createPostPersonEndpoint = sampleEndpoint.toBuilder()
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(
                    documentClassMetaDto,
                    DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto("second-database")
                )]
            )
            .build()

        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)

        def fields = payloadMetaModel.getFields()
        fields[5].fieldType = ClassMetaModelDto.builder()
            .name(documentClassMetaDto.getName())
            .classMetaModelDtoType(MetaModelDtoType.BY_NAME)
            .build()

        createPostPersonEndpoint = createPostPersonEndpoint.toBuilder()
            .baseUrl(createPostPersonEndpoint.getBaseUrl() + "/next")
            .payloadMetamodel(payloadMetaModel.toBuilder()
                .fields(fields)
                .build())
            .build()

        when:
        def endpoint = createPostPersonEndpoint.toBuilder()
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(
                    documentClassMetaDto,
                    DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto("second-database", DataStorageWithoutFactory.canonicalName)
                )]
            )
            .build()

        endpointMetaModelService.createNewEndpoint(endpoint)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("payloadMetamodel.name", messageForValidator(UniqueValue)),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage.name", messageForValidator(UniqueValue)),
            errorEntry("apiTag.name", messageForValidator(UniqueValue)),
            errorEntry("operationName", messageForValidator(UniqueValue)),
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.name", messageForValidator(UniqueValue)),
            errorEntry("dataStorageConnectors[0].dataStorageMetaModel.className", messageForValidator(VerifyThatCanCreateDataStorage)),
            errorEntry("payloadMetamodel.fields[4].fieldType.genericTypes[0].name", messageForValidator(UniqueValue)),
            errorEntry("payloadMetamodel.fields[4].fieldType.genericTypes[0].fields[2].fieldType.name", messageForValidator(UniqueValue)),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage.fields[2].fieldType.name", messageForValidator(UniqueValue)),
        ])
    }

    def "after create endpoints by id, by name and by definition in context class metamodels should be connected properly"() {
        given:
        EndpointMetaModelDto createInvoicesEndpointDto = createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("invoices")
            .operationName("createInvoice")
            .apiTag(ApiTagDto.builder()
                .name("invoices")
                .build())
            .payloadMetamodel(ClassMetaModelDto.builder()
                .translationName(sampleTranslationDto())
                .name("invoice")
                .fields([
                    createValidFieldMetaModelDto("id", Long),
                    createValidFieldMetaModelDto("code", String)
                ])
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(createInvoicesEndpointDto)

        def invoiceClassModel = metaModelContextService.getClassMetaModelByName("invoice")

        EndpointMetaModelDto createPersonEndpointDto = createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(ClassMetaModelDto.builder()
                .translationName(sampleTranslationDto())
                .name("person")
                .fields([
                    createValidFieldMetaModelDto("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("children", createListWithMetaModel(
                        buildClassMetaModelDtoWithName("person"))),
                    createValidFieldMetaModelDto("documents", createListWithMetaModel(
                        ClassMetaModelDto.builder()
                            .name("document")
                            .translationName(sampleTranslationDto())
                            .fields([
                                createValidFieldMetaModelDto("uuid", String),
                                createValidFieldMetaModelDto("serialNumber", String),
                            ])
                            .build()
                    )),
                    createValidFieldMetaModelDto("mainDocument", buildClassMetaModelDtoWithName("document")),
                    createValidFieldMetaModelDto("expiredDocuments",
                        createListWithMetaModel(buildClassMetaModelDtoWithName("document"))),
                    createValidFieldMetaModelDto("invoices",
                        createListWithMetaModel(buildClassMetaModelDtoWithId(invoiceClassModel.id))),
                    createValidFieldMetaModelDto("oldAgreements", createListWithMetaModel(
                        createClassMetaModelDtoFromClass(Agreement)
                    )),
                    createValidFieldMetaModelDto("currentAgreement",
                        createClassMetaModelDtoForClass(Agreement)
                    )
                ])
                .build())
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createPersonEndpointDto)

        then:
        def personClassModel = metaModelContextService.getClassMetaModelByName("person")
        def documentClassModel = metaModelContextService.getClassMetaModelByName("document")

        personClassModel.getFieldByName("children").getFieldType().genericTypes[0].is(personClassModel)
        personClassModel.getFieldByName("mainDocument").getFieldType().is(documentClassModel)
        personClassModel.getFieldByName("documents").getFieldType().genericTypes[0].is(documentClassModel)
        personClassModel.getFieldByName("expiredDocuments").getFieldType().genericTypes[0].is(documentClassModel)
        personClassModel.getFieldByName("invoices").getFieldType().genericTypes[0].isTheSameMetaModel(invoiceClassModel)
        def oldAgreementsFirstGenericType = personClassModel.getFieldByName("oldAgreements").getFieldType().genericTypes[0]
        def currentAgreementType = personClassModel.getFieldByName("currentAgreement").getFieldType()
        oldAgreementsFirstGenericType.is(currentAgreementType)
    }

    def "should inform about lack of full class metamodel definitions"() {
        given:
        def createInvalidPayload = createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(buildClassMetaModelDtoWithName("person"))
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(buildClassMetaModelDtoWithName("document"))
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .translationName(sampleTranslationDto())
                        .name("valid")
                        .fields([
                            createIdFieldType("id", Long)
                        ])
                        .build())
                    .build()
            ])
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createInvalidPayload)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage", messageForValidator(ExistFullDefinitionInTempContextByName)),
            errorEntry("payloadMetamodel", messageForValidator(ExistFullDefinitionInTempContextByName)),
            errorEntry("dataStorageConnectors[0].classMetaModelInDataStorage",
                createMessagePlaceholder("ClassMetaModel.id.field.not.found", "document").translateMessage())
        ])
    }

    def "should inform about lack of given id for ClassMetaModel"() {
        given:
        def createInvalidPayload = EndpointMetaModelDto.builder()
            .payloadMetamodel(buildClassMetaModelDtoWithId(12))
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createInvalidPayload)

        then:
        ConstraintViolationException tx = thrown()
        def foundErrors = ValidatorWithConverter.errorsFromViolationException(tx)

        assertValidationResults(foundErrors, [
            errorEntry("payloadMetamodel.id", createMessagePlaceholder("EntityNotFoundException.default.concrete.message",
                12, "class_meta_models").translateMessage())
        ])
    }

    @Unroll
    def "validation of service, mapper, bean name, mapper #testCase during post"() {
        given:
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("/root/{rootId}/users")
            .queryArguments(
                ClassMetaModelDto.builder()
                    .isGenericEnumType(false)
                    .fields([
                        createValidFieldMetaModelDto("requestParamName", String),
                    ])
                    .build()
            )
            .pathParams(ClassMetaModelDto.builder()
                .isGenericEnumType(false)
                .fields([
                    createValidFieldMetaModelDto("rootId", Long),
                ])
                .build())
            .payloadMetamodel(payloadMetamodel)
            .serviceMetaModel(ServiceMetaModelDto.builder()
                .serviceBeanAndMethod(serviceBean)
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(classMetaModelInDataStorage)
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                            .mapperBeanAndMethod(mapperBeanForPersist)
                            .build())
                    .build(),
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(responseClassMetaModel)
                .mapperMetaModel(
                    MapperMetaModelDto.builder()
                        .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                        .mapperBeanAndMethod(finalMapperBean)
                        .build())
                .successHttpCode(201)
                .build())
            .build()

        List<ErrorDto> foundErrors = []

        when:
        try {
            endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)
        } catch (ConstraintViolationException ex) {
            foundErrors = ValidatorWithConverter.errorsFromViolationException(ex)
        }

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        payloadMetamodel                                    | classMetaModelInDataStorage | serviceBean                   |
            mapperBeanForPersist                                           |
            finalMapperBean                                                |
            responseClassMetaModel                                                                                           |
            expectedErrors                                                                                                        | testCase

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | BeanAndMethodDto.builder()
            .className("notExistService")
            .methodName("lackMethod")
            .build()                                                                                                     ||
            BeanAndMethodDto.builder()
                .className("notExistMapper")
                .methodName("lackMethod")
                .build()                                                   |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.className", classNotExistsMessage())
        ]                                                                                                                         | "not exists service class"

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN ||
            BeanAndMethodDto.builder()
                .className("notExistMapper")
                .methodName("lackMethod")
                .build()                                                   |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.className", classNotExistsMessage())
        ]                                                                                                                         |
            "not exists mapper class"

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN.toBuilder()
            .beanName("otherName")
            .build()                                                                                                     ||
            BeanAndMethodDto.builder()
                .beanName("otherName")
                .className(SomeTestMapper.canonicalName)
                .methodName("lackMethod")
                .build()                                                   |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.beanName",
                translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.exist")),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.beanName",
                translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.exist"))
        ]                                                                                                                         |
            "not exists bean by name"

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN.toBuilder()
            .beanName("normalSpringService")
            .methodName("notExistMethodName")
            .build()                                                                                                     ||
            BeanAndMethodDto.builder()
                .beanName("mapGenericService")
                .className(SomeTestMapper.canonicalName)
                .methodName("lackMethod")
                .build()                                                   |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.beanName",
                translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.the.same.class",
                    NormalSpringService.canonicalName, DefaultGenericService.canonicalName)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                translatePlaceholder("BeansAndMethodsExistsValidator.method.not.found", DefaultGenericService.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.beanName",
                translatePlaceholder("BeansAndMethodsExistsValidator.bean.not.the.same.class",
                    MapGenericService.canonicalName, SomeTestMapper.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                translatePlaceholder("BeansAndMethodsExistsValidator.method.not.found", SomeTestMapper.canonicalName))
        ]                                                                                                                         |
            "bean types is not the same with classes"

        /*
          payload: generic_model
          service:
             input: invalid (Map<String, Long>)
             return: invalid (Map<String, Object>)
          responseMetaModelClass: long
         */

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "method1")                                                |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType("java.util.Map<java.lang.String, java.lang.Object>",
                    Long.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(1, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(3, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(4, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(5, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(6, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(7, BeanType.SERVICE))
        ]                                                                                                                         |
            "payload generic_model, service invalid return types and arguments"

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "method2")                                                |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload generic_model, service valid return types and arguments"

        /*
         * payload: generic_model
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Long)
         *    return: invalid (List<realDto>)
         * classMetaModelInDs: other_generic_model, id:String
         * responseMetaModelMapper:
         *    input: invalid (LocalDateTime)
         *    return: invalid (List<realDto>)
         * responseMetaModelClass: generic_model
          */

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethod1")        |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethod2")        | PERSON_METAMODEL                                | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(1, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(3, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(4, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(5, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(6, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(7, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(10, BeanType.MAPPER)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType("java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>", 'person_ds')),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType("java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>", 'person')),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),

        ]                                                                                                                         |
            "payload generic_model, inner and final mappers invalid return types and arguments"

        PERSON_METAMODEL                                    | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethod3")        |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid1")    | PERSON_METAMODEL                                | [] |
            "payload generic_model, inner and final mappers valid return types and arguments"

        /*
        * payload: list<generic_model>
        * service:
        *    input: invalid (simple type)
        *    return: invalid (Map<String, Object>)
        * responseMetaModelClass: generic_enum
        *
        */

        LIST_OF_PERSON_METAMODEL                            | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod3")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | GENERIC_ENUM                                    | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType("java.util.Map<java.lang.String, java.lang.Object>",
                    "exampleEnum")
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE))
        ]                                                                                                                         |
            "payload list<generic_model>, service invalid return types and arguments"

        LIST_OF_PERSON_METAMODEL                            | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod3")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | GENERIC_ENUM                                    | [] |
            "payload list<generic_model>, service valid return types and arguments"

        /*
        * payload: list<generic_model>
        * service: DEFAULT_GENERIC_SERVICE_BEAN
        * mapperMetaModelForPersist:
        *    input: invalid (Long)
        *    return: invalid (List<realDto>)
        * classMetaModelInDs: other_generic_model, id:String
        * responseMetaModelMapper:
        *    input: invalid (Long)
        *    return: invalid (List<realDto>)
        * responseMetaModelClass: List<generic_model>
        */

        LIST_OF_PERSON_METAMODEL                            | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid1") |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid1") | LIST_OF_PERSON_METAMODEL                        | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    'person_ds')),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    'java.util.List<java.util.Map<java.lang.String, java.lang.Object>>')),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            " payload list<generic_model>, inner and final mappers invalid return types and arguments"

        LIST_OF_PERSON_METAMODEL                            | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid1")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid1")    | PERSON_METAMODEL                                | [] |
            " payload list<generic_model>, inner and final mappers valid return types and arguments"

        /*
        * payload: realDto
        * service:
        *    input: invalid (Long)
        *    return: invalid(responseEntity<Long>)
        * responseMetaModelClass: String
        */

        REAL_DTO                                            | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod4")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(String)        | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType("java.lang.Long",
                    String.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(1, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(2, BeanType.SERVICE)),
        ]                                                                                                                         |
            "payload realDto, service invalid return types and arguments"

        REAL_DTO                                            | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod4")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(String)        | [] |
            "payload realDto, service valid return types and arguments"

        /*
        * payload: realDto
        * service: DEFAULT_GENERIC_SERVICE_BEAN
        * mapperMetaModelForPersist:
        *    input: invalid (Map<String, Object>)
        *    return: invalid (List<realDto>)
        * classMetaModelInDs: other_generic_model, id:String
         * responseMetaModelMapper:
        *    input: invalid (Long)
        *    return: invalid (List<realDto>)
        * responseMetaModelClass: Map<String, generic_model>
        */

        REAL_DTO                                            | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid2") |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid3") |
            createClassMetaModelDtoWithGenerics(Map,
                createClassMetaModelDtoFromClass(String),
                PERSON_METAMODEL
            )                                                                                                                | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    'person_ds')),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    'java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>')),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload realDto, inner and final mappers invalid return types and arguments"

        REAL_DTO                                            | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid3")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid2")    |
            createClassMetaModelDtoWithGenerics(Map,
                createClassMetaModelDtoFromClass(String),
                PERSON_METAMODEL
            )                                                                                                                | [] |
            "payload realDto, inner and final mappers valid return types and arguments"

        /*
         * payload: list<realDto>,
         * service:
         *    input: invalid (Map<String, Object>)
         *    return: invalid Long
         * responseMetaModelClass: String
         */

        createClassMetaModelDtoWithGenerics(List, REAL_DTO) | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod5")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(String)        | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType(Long.canonicalName, String.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(1, BeanType.SERVICE)),
        ]                                                                                                                         |
            "payload list<realDto>, service invalid return types and arguments"

        createClassMetaModelDtoWithGenerics(List, REAL_DTO) | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod5")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(String)        | [] |
            "payload list<realDto>, service valid return types and arguments"

        /*
         * payload: list<realDto>,
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Map<String, Object>)
         *    return: invalid (List<realDto>)
         * classMetaModelInDs: other_generic_model, id:String
           * responseMetaModelMapper:
         *    input: invalid (Long)
         *    return: invalid (List<realDto>)
         * responseMetaModelClass: Long
         */

        createClassMetaModelDtoWithGenerics(List, REAL_DTO) | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid2") |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid3") |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    'person_ds')),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    Long.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload list<realDto>, inner and final mappers invalid return types and arguments"

        createClassMetaModelDtoWithGenerics(List, REAL_DTO) | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid4")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid3")    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload list<realDto>, inner and final mappers valid return types and arguments"

        /*
         * payload: Long,
         * service:
         *    input: (invalid) realDto
         *    return: (invalid) String
         * responseMetaModelClass: Long
         */

        createClassMetaModelDtoFromClass(Long)              | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod6")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(Long)          | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType(String.canonicalName, Long.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
        ]                                                                                                                         |
            "payload Long, service invalid return types and arguments"

        createClassMetaModelDtoFromClass(Long)              | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod6")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(Long)          | [] |
            "payload Long, service valid return types and arguments"

        /*
         * payload: Long,
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Map<String, Object>)
         *    return: invalid (List<realDto>)
         * classMetaModelInDs: realDto, id:Integer
          * responseMetaModelMapper:
         *    input: invalid (Long)
         *    return: invalid (realDto)
         * responseMetaModelClass: Long
         */

        createClassMetaModelDtoFromClass(Long)              | REAL_DTO                    | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid4") |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalInvalid1")  |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    SomeRawDto.canonicalName)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    SomeRawDto.canonicalName,
                    Long.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload Long, inner and final mappers invalid return types and arguments"

        createClassMetaModelDtoFromClass(Long)              | REAL_DTO                    | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid5")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid4")    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload Long, inner and final mappers valid return types and arguments"

        /*
         * payload: generic_enum,
         * service:
         *    input: invalid realDto
         *    return: invalid String
         * responseMetaModelClass: LocalDateTime
         */

        GENERIC_ENUM                                        | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod7")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(LocalDateTime) | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType(String.canonicalName, LocalDateTime.canonicalName)
            ), errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
            translatePlaceholder("BeansAndMethodsExistsValidator.invalid.method.argument",
                0, "{BeansAndMethodsExistsValidator.service.type}")
            ),
        ]                                                                                                                         |
            "payload generic_enum, service invalid return types and arguments"

        GENERIC_ENUM                                        | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod7")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    | createClassMetaModelDtoFromClass(LocalDateTime) | [] |
            "payload generic_enum, service valid return types and arguments"

        /*
         * payload: generic_enum,
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Map<String, Long>)
         *    return: invalid (Map<String, Object>)
         * classMetaModelInDs: real_dto
          * responseMetaModelMapper:
         *    input: invalid (Long)
         *    return: invalid (List<realDto>)
         * responseMetaModelClass: Long
         */

        GENERIC_ENUM                                        | REAL_DTO                    | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid6") |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalInvalid6")  |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    "java.util.Map<java.lang.String, java.lang.Object>",
                    SomeRawDto.canonicalName)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    Long.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload generic_enum, inner and final mappers invalid return types and arguments"

        GENERIC_ENUM                                        | REAL_DTO                    | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid6")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid6")    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload generic_enum, inner and final mappers invalid return types and arguments"

        /*
         * payload: enum,
         * service:
         *    input: invalid (realDto)
         *    return: invalid (String)
         * responseMetaModelClass: Long
         */

        createClassMetaModelDtoFromClass(SomeEnum1)         | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod7")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType(String.canonicalName, Long.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
        ]                                                                                                                         |
            "payload enum, service invalid return types and arguments"

        createClassMetaModelDtoFromClass(SomeEnum1)         | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod8")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload enum, service valid return types and arguments"

        /*
         * payload: enum,
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Map<String, Long>)
         *    return: invalid (realDto)
         * classMetaModelInDs: other_generic_model, id:String
         * responseMetaModelMapper:
         *    input: invalid (Long)
         *    return: invalid (List<realDto>)
         * responseMetaModelClass: enum
         */
        createClassMetaModelDtoFromClass(SomeEnum1)         | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid7")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalInvalid6")  |
            createClassMetaModelDtoFromClass(SomeEnum1)                                                                      | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    SomeRawDto.canonicalName,
                    'person_ds'
                )),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    SomeEnum1.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload enum, inner and final mappers invalid return types and arguments"

        createClassMetaModelDtoFromClass(SomeEnum1)         | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid8")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid8")    |
            createClassMetaModelDtoFromClass(SomeEnum1)                                                                      | [] |
            "payload enum, inner and final mappers valid return types and arguments"

        /*
         * payload: map<string, long>,
         * service:
         *    input: invalid (realDto)
         *    return:invalid (String)
         * responseMetaModelClass: Long
         */

        createClassMetaModelDtoWithGenerics(Map,
            createClassMetaModelDtoFromClass(String),
            createClassMetaModelDtoFromClass(Long)
        )                                                   | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "invalidMethod9")                                         |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodReturnType(String.canonicalName, Long.canonicalName)
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
        ]                                                                                                                         |
            "payload map<string, long>, service invalid return types and arguments"

        createClassMetaModelDtoWithGenerics(Map,
            createClassMetaModelDtoFromClass(String),
            createClassMetaModelDtoFromClass(Long)
        )                                                   | PERSON_METAMODEL_DS         |
            createBeanAndMethodDto(BeanAndMethodDtoTestService, "validMethod9")                                           |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            DEFAULT_GENERIC_MAPPER_BEAN                                    |
            createClassMetaModelDtoFromClass(Long)                                                                           | [] |
            "payload map<string, long>, service invalid return types and arguments"

        /*
         * payload: map<string, long>,
         * service: DEFAULT_GENERIC_SERVICE_BEAN
         * mapperMetaModelForPersist:
         *    input: invalid (Map<String, String>)
         *    return: invalid (Long)
         * classMetaModelInDs: other_generic_model, id:String
         * responseMetaModelMapper:
         *    input: invalid (Long)
         *    return: invalid (List<realDto>)
         * responseMetaModelClass: enum
         */

        createClassMetaModelDtoWithGenerics(Map,
            createClassMetaModelDtoFromClass(String),
            createClassMetaModelDtoFromClass(Long)
        )                                                   | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodInvalid9") |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalInvalid9")  |
            createClassMetaModelDtoFromClass(SomeEnum1)                                                                      | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodReturnType(
                    Long.canonicalName,
                    'person_ds'
                )),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    "java.util.List<pl.jalokim.crudwizard.genericapp.rest.samples.dto.SomeRawDto>",
                    SomeEnum1.canonicalName)),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER)),
        ]                                                                                                                         |
            "payload map<string, long>, inner and final mappers invalid return types and arguments"

        createClassMetaModelDtoWithGenerics(Map,
            createClassMetaModelDtoFromClass(String),
            createClassMetaModelDtoFromClass(Long)
        )                                                   | PERSON_METAMODEL_DS         | DEFAULT_GENERIC_SERVICE_BEAN  |
            createBeanAndMethodDto(SomeTestMapper, "mapperMethodValid9")   |
            createBeanAndMethodDto(SomeTestMapper, "mapperFinalValid9")    |
            createClassMetaModelDtoFromClass(SomeEnum1)                                                                      | [] |
            "payload map<string, long>, inner and final mappers valid return types and arguments"
    }

    @Unroll
    def "validation final mappers for return list or page #testCase during get"() {
        given:
        def createEndpointMetaModelDto = EndpointMetaModelDto.builder()
            .baseUrl("users")
            .operationName("getUsers")
            .apiTag(ApiTagDto.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.GET)
            .payloadMetamodel(null)
            .dataStorageConnectors(dataStorageConnectors)
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(responseClassMetaModel)
                .mapperMetaModel(MapperMetaModelDto.builder()
                    .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                    .mapperBeanAndMethod(finalMapperBean)
                    .build())
                .successHttpCode(201)
                .build()
            )
            .dataStorageResultsJoiners(resultJoiners)
            .build()

        List<ErrorDto> foundErrors = []

        when:
        try {
            endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)
        } catch (ConstraintViolationException ex) {
            foundErrors = ValidatorWithConverter.errorsFromViolationException(ex)
        }

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        dataStorageConnectors | resultJoiners                  | finalMapperBean                                                 | responseClassMetaModel   |
            expectedErrors |
            testCase
        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(SomeRawDto))
                .build()
        ]                     | null                           | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryValid1")   | LIST_OF_PERSON_METAMODEL |
            []             |
            "fine last mapper for list when one ds"
        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(SomeRawDto))
                .build()
        ]                     | null                           | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryInvalid1") | LIST_OF_PERSON_METAMODEL | [
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    SomeRawDto.canonicalName,
                    "person")),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]                  |
            "invalid last mapper for list when one ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL)
                .nameOfQuery("firstDs")
                .build(),
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL_DS)
                .nameOfQuery("secondDs")
                .build(),
        ]                     | [joinerEntry("id", "otherId")] | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryValid2")   |
            createClassMetaModelDtoWithGenerics(List,
                createClassMetaModelDtoFromClass(SomeRawDto))                                                                                               |
            []             |
            "fine last mapper for list when more ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL)
                .nameOfQuery("firstDs")
                .build(),
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL_DS)
                .nameOfQuery("secondDs")
                .build(),
        ]                     | [joinerEntry("id", "otherId")] | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryInvalid2") |
            createClassMetaModelDtoWithGenerics(List,
                createClassMetaModelDtoFromClass(SomeRawDto))                                                                                               | [
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    Long.canonicalName,
                    SomeRawDto.canonicalName)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]                  |
            "invalid last mapper for list when more ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(SomeRawDto))
                .build()
        ]                     | null                           | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryValid1")   | PAGE_OF_PERSON_METAMODEL |
            []             |
            "fine last mapper for page when one ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(SomeRawDto))
                .build()
        ]                     | null                           | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryInvalid1") | PAGE_OF_PERSON_METAMODEL | [
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    SomeRawDto.canonicalName,
                    "person")),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]                  |
            "invalid last mapper for list when one ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL)
                .nameOfQuery("firstDs")
                .build(),
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL_DS)
                .nameOfQuery("secondDs")
                .build(),
        ]                     | [joinerEntry("id", "otherId")] | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryValid2")   |
            createClassMetaModelDtoWithGenerics(Page,
                createClassMetaModelDtoFromClass(SomeRawDto))                                                                                               |
            []             |
            "fine last mapper for page when more ds"

        // -----------------------------------------------------
        [
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL)
                .nameOfQuery("firstDs")
                .build(),
            DataStorageConnectorMetaModelDto.builder()
                .classMetaModelInDataStorage(PERSON_METAMODEL_DS)
                .nameOfQuery("secondDs")
                .build(),
        ]                     | [joinerEntry("id", "otherId")] | createBeanAndMethodDto(SomeTestMapper, "finalMapEntryInvalid2") |
            createClassMetaModelDtoWithGenerics(Page,
                createClassMetaModelDtoFromClass(SomeRawDto))                                                                                               | [
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodReturnType(
                    Long.canonicalName,
                    SomeRawDto.canonicalName)),
            errorEntry("responseMetaModel.mapperMetaModel.mapperBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]                  |
            "invalid last mapper for page when more ds"
    }

    @Unroll
    def "validation query mappers for return one object during get, test case: #testCase"() {
        given:
        def createEndpointMetaModelDto = EndpointMetaModelDto.builder()
            .baseUrl("users/{userId}")
            .operationName("getUser")
            .apiTag(ApiTagDto.builder()
                .name("users")
                .build())
            .httpMethod(HttpMethod.GET)
            .pathParams(ClassMetaModelDto.builder()
                .fields([
                    createValidFieldMetaModelDto("userId", Long)
                ])
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(PERSON_METAMODEL)
                    .nameOfQuery("firstDs")
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(PERSON_METAMODEL_DS)
                    .mapperMetaModelForQuery(
                        MapperMetaModelDto.builder()
                            .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                            .mapperBeanAndMethod(queryMapper)
                            .build()
                    )
                    .nameOfQuery("secondDs")
                    .build(),
            ])
            .responseMetaModel(EndpointResponseMetaModelDto.builder()
                .classMetaModel(createClassMetaModelDtoFromClass(SomeRawDto))
                .mapperMetaModel(MapperMetaModelDto.builder()
                    .mapperType(MapperType.BEAN_OR_CLASS_NAME)
                    .mapperBeanAndMethod(
                        createBeanAndMethodDto(SomeTestMapper, "finalMapEntryValid3"))
                    .build())
                .successHttpCode(201)
                .build()
            )
            .build()

        List<ErrorDto> foundErrors = []

        when:
        try {
            endpointMetaModelService.createNewEndpoint(createEndpointMetaModelDto)
        } catch (ConstraintViolationException ex) {
            foundErrors = ValidatorWithConverter.errorsFromViolationException(ex)
        }

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        queryMapper                                             | expectedErrors | testCase
        createBeanAndMethodDto(SomeTestMapper, "mapIdValid1")   | []             | "fine query mappers for get one object"
        createBeanAndMethodDto(SomeTestMapper, "mapIdInvalid2") | [
            errorEntry("dataStorageConnectors[1].mapperMetaModelForQuery.methodName",
                invalidMethodReturnType(Long.canonicalName, String.canonicalName)),
            errorEntry("dataStorageConnectors[1].mapperMetaModelForQuery.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]                                                                        | "invalid query mappers for get one object"
    }

    @Unroll
    def "return expected errors during generate mappers code #testCase"() {
        given:
        def postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .translationName(sampleTranslationDto())
                        .name("personEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("code", String),
                            createValidFieldMetaModelDto("personEvent", PersonEvent),
                            createValidFieldMetaModelDto("forInnerMethod", String),
                            createValidFieldMetaModelDto("bySpringExpression", String),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("personEvent")
                                            .sourceAssignExpression(mapperExpression)
                                            .build(),
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("forInnerMethod")
                                            .sourceAssignExpression(innerMethodNameExpression)
                                            .build(),
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("bySpringExpression")
                                            .sourceAssignExpression(bySpringBeanExpression)
                                            .build(),
                                    ])
                                    .build())
                                .subMappersAsMethods([MapperConfigurationDto.builder()
                                                          .name("mapIdToText")
                                                          .sourceMetaModel(createClassMetaModelDtoFromClass(Long))
                                                          .targetMetaModel(createClassMetaModelDtoFromClass(String))
                                                          .build()])
                                .build())
                            .build())
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(createClassMetaModelDtoFromClass(PersonEvent))
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personEventMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(
                                    MapperConfigurationDto.builder()
                                        .name("personEventMapper")
                                        .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                        .targetMetaModel(createClassMetaModelDtoForClass(PersonEvent))
                                        .build()
                                )
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personDto")
                .translationName(sampleTranslationDto())
                .fields([
                    createValidFieldMetaModelDto("id", Long),
                    createValidFieldMetaModelDto("code", String),
                ])
                .build())
            .build()
        def foundErrors = []

        when:
        try {
            endpointMetaModelService.createNewEndpoint(postEndpoint)
        } catch (ConstraintViolationException ex) {
            foundErrors = ValidatorWithConverter.errorsFromViolationException(ex)
        }

        then:
        assertValidationResults(foundErrors, expectedErrors)

        where:
        mapperExpression                        | innerMethodNameExpression | bySpringBeanExpression              | expectedErrors | testCase
        '@personEventMapper($rootSourceObject)' | "#mapIdToText(id)"        | "@dummyService.getSomeRandomText()" | []             |
            "generate mappers correctly, real spring bean, real innerMapper, real other mapper in the same context created"

        '@notExistMapper($rootSourceObject)'    | "#notExistMethod(id)"     | "@springBean.getSomeRandomText()"   | [
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist." +
                "mapperGenerateConfiguration.rootConfiguration.propertyOverriddenMapping[1].sourceAssignExpression",
                parseExpressionMessage(19,
                    translatePlaceholder("cannot.find.method.with.arguments",
                        [
                            methodName  : "notExistMethod",
                            classesTypes: Long.canonicalName,
                            givenClass  : "{current.mapper.name}"]
                    ))
            ),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist." +
                "mapperGenerateConfiguration.rootConfiguration.propertyOverriddenMapping[2].sourceAssignExpression",
                parseExpressionMessage(13,
                    translatePlaceholder("cannot.find.bean.name", "springBean"))
            ),
            errorEntry("dataStorageConnectors[0].mapperMetaModelForPersist." +
                "mapperGenerateConfiguration.rootConfiguration.propertyOverriddenMapping[0].sourceAssignExpression",
                parseExpressionMessage(17,
                    translatePlaceholder("MappersModelsCache.not.found.mapper", "notExistMapper"))
            )
        ]                                                                                                                          |
            "like above but with errors"
    }

    def "use mapper name created earlier in other endpoint"() {
        given:
        def postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("code", String),
                            createValidFieldMetaModelDto("createdDate", LocalDate),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("createdDate")
                                            .sourceAssignExpression("created")
                                            .build(),
                                    ])
                                    .build())
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .translationName(sampleTranslationDto())
                .name("personDto")
                .fields([
                    createIdFieldType("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("created", LocalDate),
                    createValidFieldMetaModelDto("otherCode", String),
                ])
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(postEndpoint)

        def putEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .baseUrl("wrapper-people")
            .operationName("createPerson")
            .apiTag(ApiTagDto.builder()
                .name("wrapper-people")
                .build())
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personWrapperEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("uuid", String),
                            createValidFieldMetaModelDto("person", buildClassMetaModelDtoWithName("personEntity")),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personWrapperDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personWrapperDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personWrapperDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personWrapperEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("person")
                                            .sourceAssignExpression("@personDtoToEntityMapper(person)")
                                            .build(),
                                    ])
                                    .build())
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personWrapperDto")
                .translationName(sampleTranslationDto())
                .fields([
                    createIdFieldType("id", Long),
                    createValidFieldMetaModelDto("uuid", String),
                    createValidFieldMetaModelDto("person", buildClassMetaModelDtoWithName("personDto")),
                ])
                .build())
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(putEndpoint)

        then:
        noExceptionThrown()
    }

    def "return the same generated mapper instance when should or generate new one"() {
        given:
        def postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("code", String),
                            createValidFieldMetaModelDto("createdDate", LocalDate),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("createdDate")
                                            .sourceAssignExpression("created")
                                            .build(),
                                    ])
                                    .build())
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .translationName(sampleTranslationDto())
                .name("personDto")
                .fields([
                    createIdFieldType("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("created", LocalDate),
                    createValidFieldMetaModelDto("otherCode", String),
                ])
                .build())
            .build()

        long endpointId = endpointMetaModelService.createNewEndpoint(postEndpoint)
        String firstCompiledMapperHash = null
        String firstMapperFullClassName = null
        String firstMapperFullFilePath = null
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.getOne(endpointId)
            def mapperEntity = endpointEntity.getDataStorageConnectors().get(0).getMapperMetaModelForPersist()
            def compiledMetadata = mapperEntity.getMapperGenerateConfiguration().getMapperCompiledCodeMetadata()
            firstCompiledMapperHash = compiledMetadata.generatedCodeHash
            firstMapperFullClassName = compiledMetadata.fullClassName
            firstMapperFullFilePath = compiledMetadata.fullPath
            return
        }
        def metaModelContext = metaModelContextService.getMetaModelContext()
        def mapperModel = metaModelContext.getMapperMetaModels().getMapperMetaModelByName("personDtoToEntityMapper")
        def firstMapperInstance = mapperModel.mapperInstance

        when: 'noting changed so generated code should be the same'
        metaModelContextService.reloadAll()

        then:
        Files.exists(Path.of(firstMapperFullFilePath))
        Files.exists(Path.of(firstMapperFullFilePath.replace(".class", ".java")))
        def afterFirstReloadContext = metaModelContextService.getMetaModelContext()
        def afterFirstReloadMapperModel = afterFirstReloadContext.getMapperMetaModels().getMapperMetaModelByName("personDtoToEntityMapper")
        def afterFirstReloadMapperInstance = afterFirstReloadMapperModel.mapperInstance
        String afterFirstReloadCompiledMapperHash = null
        String afterFirstReloadMapperFullClassName = null
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.getOne(endpointId)
            def mapperEntity = endpointEntity.getDataStorageConnectors().get(0).getMapperMetaModelForPersist()
            def compiledMetadata = mapperEntity.getMapperGenerateConfiguration().getMapperCompiledCodeMetadata()
            afterFirstReloadCompiledMapperHash = compiledMetadata.generatedCodeHash
            afterFirstReloadMapperFullClassName = compiledMetadata.fullClassName
            return
        }
        afterFirstReloadCompiledMapperHash == firstCompiledMapperHash
        afterFirstReloadMapperFullClassName == firstMapperFullClassName
        afterFirstReloadMapperInstance == firstMapperInstance

        and: 'changed class metamodel so mappers should be new one generated'
        def newMetaModelContext = metaModelContextService.getMetaModelContext()
        inTransaction {
            def personDtoClassMetaModel = newMetaModelContext.findClassMetaModelByName("personDto")
            def personEntityClassMetaModel = newMetaModelContext.findClassMetaModelByName("personEntity")
            def personDtoClassEntity = classMetaModelRepository.getOne(personDtoClassMetaModel.id)
            def personEntityClassEntity = classMetaModelRepository.getOne(personEntityClassMetaModel.id)
            personDtoClassEntity.getFields().find {
                it.fieldName == 'code'
            }.fieldName = "newCode"
            personEntityClassEntity.getFields().find {
                it.fieldName == 'code'
            }.fieldName = "newCode"
            return
        }

        when:
        metaModelContextService.reloadAll()

        then:
        !Files.exists(Path.of(firstMapperFullFilePath))
        !Files.exists(Path.of(firstMapperFullFilePath.replace(".class", ".java")))
        def afterSecondReloadContext = metaModelContextService.getMetaModelContext()
        def afterSecondReloadMapperModel = afterSecondReloadContext.getMapperMetaModels().getMapperMetaModelByName("personDtoToEntityMapper")
        def afterSecondReloadMapperInstance = afterSecondReloadMapperModel.mapperInstance
        String afterSecondReloadCompiledMapperHash = null
        String afterSecondReloadMapperFullClassName = null
        String afterSecondReloadMapperFullFilePath = null
        inTransaction {
            def endpointEntity = endpointMetaModelRepository.getOne(endpointId)
            def mapperEntity = endpointEntity.getDataStorageConnectors().get(0).getMapperMetaModelForPersist()
            def compiledMetadata = mapperEntity.getMapperGenerateConfiguration().getMapperCompiledCodeMetadata()
            afterSecondReloadCompiledMapperHash = compiledMetadata.generatedCodeHash
            afterSecondReloadMapperFullClassName = compiledMetadata.fullClassName
            afterSecondReloadMapperFullFilePath = compiledMetadata.fullPath
            return
        }
        Files.exists(Path.of(afterSecondReloadMapperFullFilePath))
        Files.exists(Path.of(afterSecondReloadMapperFullFilePath.replace(".class", ".java")))
        afterSecondReloadCompiledMapperHash != firstCompiledMapperHash
        afterSecondReloadMapperFullClassName != firstMapperFullClassName
        afterSecondReloadMapperInstance != firstMapperInstance
    }

    def "after not passed validation temp files should be removed"() {
        given:
        fixedInstant(Instant.ofEpochMilli(1668361280915))
        def postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("code", String),
                            createValidFieldMetaModelDto("createdDate", LocalDate),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("createdDate")
                                            .sourceAssignExpression("created")
                                            .build(),
                                    ])
                                    .build())
                                .build())
                            .build())
                    .build(),
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity2")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("uuid", String),
                            createValidFieldMetaModelDto("created", LocalDate),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper2")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper2")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity2"))
                                    .build())
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personDto")
                .fields([
                    createIdFieldType("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("created", LocalDate),
                    createValidFieldMetaModelDto("otherCode", String),
                ])
                .build())
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(postEndpoint)

        then:
        ConstraintViolationException ex = thrown()
        ex.message != null
        !Files.exists(Path.of(codeRootPathProvider.compiledCodeRootPath + "/1668361280915"))
    }

    def "should merge fields properties correctly and enum translations"() {
        given:
        def postEndpoint
        postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
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
                        createValidFieldMetaModelDto("someObject", createClassMetaModelDtoFromClass(NestedObject).toBuilder()
                            .fields([
                                createValidFieldMetaModelDto("objectName", String)
                            ])
                            .build()),
                        createValidFieldMetaModelDto("id", Long, [notNullValidatorMetaModelDto()]),
                        createValidFieldMetaModelDto("someType", SomeEnumTranslations).toBuilder()
                            .fieldType(createClassMetaModelDtoFromClass(SomeEnumTranslations).toBuilder()
                                .enumMetaModel(
                                    EnumMetaModelDto.builder()
                                        .enums([
                                            sampleEntryMetaModel("SIMPLE"),
                                            sampleEntryMetaModel("MEDIUM"),
                                            sampleEntryMetaModel("FULL"),
                                        ])
                                        .build()
                                )
                                .build())
                            .build()
                    ])
                    .build()
            )
            .build()

        when:
        def endpointId = endpointMetaModelService.createNewEndpoint(postEndpoint)

        then:
        endpointId >= 0
        def metaModelContext = metaModelContextService.metaModelContext
        def foundClassModel = metaModelContext.classMetaModels.findOneBy {
            ObjectForMergeTranslations == it.realClass
        }

        def fieldName = foundClassModel.fetchAllFields().find {
            it.fieldName == "name"
        }
        fieldName.fieldType.realClass == String
        fieldName.getAdditionalProperties()[0].valueAsObject == "some-value"
        CollectionUtils.isEmpty(fieldName.validators)
        fieldName.translationFieldName.translationKey == "field.${ObjectForMergeTranslations.canonicalName}.name"

        def fieldId = foundClassModel.fetchAllFields().find {
            it.fieldName == "id"
        }
        fieldId.fieldType.realClass == Long
        CollectionUtils.isEmpty(fieldId.getAdditionalProperties())
        fieldId.validators*.realClass == [NotNullValidator]
        fieldId.translationFieldName.translationKey == "field.${ObjectForMergeTranslations.canonicalName}.id"

        def fieldSomeObject = foundClassModel.fetchAllFields().find {
            it.fieldName == "someObject"
        }
        fieldSomeObject.fieldType.realClass == NestedObject
        fieldSomeObject.fieldType.translationName.translationKey == "classMetaModel.${NestedObject.canonicalName}"
        fieldSomeObject.translationFieldName.translationKey == "field.${ObjectForMergeTranslations.canonicalName}.someObject"

        def fieldObjectName = fieldSomeObject.fieldType.fetchAllFields().find {
            it.fieldName == "objectName"
        }
        fieldObjectName.fieldType.realClass == String
        fieldObjectName.translationFieldName.translationKey == "field.${NestedObject.canonicalName}.objectName"

        def fieldSomeType = foundClassModel.fetchAllFields().find {
            it.fieldName == "someType"
        }
        fieldSomeType.fieldType.realClass == SomeEnumTranslations
        def enumMetaModel = fieldSomeType.fieldType.enumMetaModel
        enumMetaModel.getEnumByName("SIMPLE").getTranslation().translationKey == "enum.${SomeEnumTranslations.canonicalName}.SIMPLE"
        enumMetaModel.getEnumByName("MEDIUM").getTranslation().translationKey == "enum.${SomeEnumTranslations.canonicalName}.MEDIUM"
        enumMetaModel.getEnumByName("FULL").getTranslation().translationKey == "enum.${SomeEnumTranslations.canonicalName}.FULL"
    }

    private static String parseExpressionMessage(int columnNumber, String message) {
        translatePlaceholder("MapperContextEntryError.column") + ":" + columnNumber + ", " + message
    }

    private static void assertFoundOneClassMetaModel(List<ClassMetaModel> classMetaModels, Predicate<ClassMetaModel> predicate) {
        def found = classMetaModels.findAll {
            predicate.test(it)
        }
        assert found.size() == 1
    }

    private static final PERSON_METAMODEL = ClassMetaModelDto.builder()
        .name("person")
        .translationName(sampleTranslationDto())
        .fields([
            createIdFieldType("id", Long),
            createValidFieldMetaModelDto("name", String),
            createValidFieldMetaModelDto("surname", String)
        ])
        .build()

    private static final LIST_OF_PERSON_METAMODEL = createClassMetaModelDtoWithGenerics(List, PERSON_METAMODEL)

    private static final PAGE_OF_PERSON_METAMODEL = createClassMetaModelDtoWithGenerics(Page, PERSON_METAMODEL)

    private static final PERSON_METAMODEL_DS = ClassMetaModelDto.builder()
        .translationName(sampleTranslationDto())
        .name("person_ds")
        .fields([
            createIdFieldType("id", String),
            createValidFieldMetaModelDto("name", String),
            createValidFieldMetaModelDto("surname", String),
            createValidFieldMetaModelDto("otherId", Long)
        ])
        .build()

    private static final GENERIC_ENUM = createEnumMetaModel("VAL1", "VAL2")

    private static final REAL_DTO = createClassMetaModelDtoFromClass(SomeRawDto)

    private static final DEFAULT_GENERIC_SERVICE_BEAN = BeanAndMethodDto.builder()
        .className(DefaultGenericService.canonicalName)
        .methodName("saveOrReadFromDataStorages")
        .build()

    private static final DEFAULT_GENERIC_MAPPER_BEAN = BeanAndMethodDto.builder()
        .className(DefaultGenericMapper.canonicalName)
        .methodName("mapToTarget")
        .build()

    private static BeanAndMethodDto createBeanAndMethodDto(Class<?> realClass, String methodName) {
        BeanAndMethodDto.builder()
            .className(realClass.canonicalName)
            .methodName(methodName)
            .build()
    }

    private static String invalidMethodReturnType(String methodReturns, String expectedType) {
        translatePlaceholder("BeansAndMethodsExistsValidator.method.return.type.invalid",
            methodReturns, expectedType)
    }

    private static String invalidMethodParameter(int index, BeanType beanType) {
        String beanTypeKey = beanType == BeanType.SERVICE ? "service" : "mapper"
        translatePlaceholder("BeansAndMethodsExistsValidator.invalid.method.argument",
            index, "{BeansAndMethodsExistsValidator." + beanTypeKey + ".type}")
    }

    private enum BeanType {
        SERVICE,
        MAPPER
    }

    private static DataStorageResultsJoinerDto joinerEntry(String leftPath, String rightPath) {
        DataStorageResultsJoinerDto.builder()
            .leftNameOfQueryResult("firstDs")
            .leftPath(leftPath)
            .rightPath(rightPath)
            .rightNameOfQueryResult('secondDs')
            .joinerVerifierClassName(EqObjectsJoiner.canonicalName)
            .build()
    }
}
