package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoWithGenerics
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createEnumMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel1
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel.PLACEHOLDER_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators2
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService.createNewEndpointReason
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.classNotExistsMessage
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.function.Predicate
import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.http.HttpMethod
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.rest.response.error.ErrorDto
import pl.jalokim.crudwizard.core.sample.Agreement
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue
import pl.jalokim.crudwizard.genericapp.customendpoint.SomeCustomRestController
import pl.jalokim.crudwizard.genericapp.mapper.DefaultGenericMapper
import pl.jalokim.crudwizard.genericapp.mapper.conversion.SomeEnum1
import pl.jalokim.crudwizard.genericapp.mapper.instance.SomeTestMapper
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
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
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto
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
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
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
        def createPostPersonEndpoint = createValidPostExtendedUserWithValidators2().toBuilder()
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(
                    ClassMetaModelDtoSamples.createDocumentClassMetaDto(),
                    DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto("second-database")
                )]
            )
            .build()

        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)

        createPostPersonEndpoint = createPostPersonEndpoint.toBuilder()
            .baseUrl(createPostPersonEndpoint.getBaseUrl() + "/next")
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint.toBuilder()
            .dataStorageConnectors([
                createSampleDataStorageConnectorDto(
                    ClassMetaModelDtoSamples.createDocumentClassMetaDto(),
                    DataStorageMetaModelDtoSamples.createDataStorageMetaModelDto("second-database", DataStorageWithoutFactory.canonicalName)
                )]
            )
            .build())

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
                .name("person")
                .fields([
                    createValidFieldMetaModelDto("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("children", createListWithMetaModel(
                        buildClassMetaModelDtoWithName("person"))),
                    createValidFieldMetaModelDto("documents", createListWithMetaModel(
                        ClassMetaModelDto.builder()
                            .name("document")
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
                        ClassMetaModelDto.builder()
                            .className(Agreement.canonicalName)
                            .build()
                    )),
                    createValidFieldMetaModelDto("currentAgreement",
                        ClassMetaModelDto.builder()
                            .className(Agreement.canonicalName)
                            .build()
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
        personClassModel.getFieldByName("oldAgreements").getFieldType().genericTypes[0]
            .is(personClassModel.getFieldByName("currentAgreement").getFieldType())
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
                invalidMethodParameter(2, BeanType.SERVICE)),
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
                invalidMethodParameter(2, BeanType.MAPPER)),
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
            ),
            errorEntry("serviceMetaModel.serviceBeanAndMethod.methodName",
                invalidMethodParameter(0, BeanType.SERVICE)),
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
        queryMapper                                           | expectedErrors | testCase
        createBeanAndMethodDto(SomeTestMapper, "mapIdValid1") | []             | "fine query mappers for get one object"
        createBeanAndMethodDto(SomeTestMapper, "mapIdInvalid2") | [
            errorEntry("dataStorageConnectors[1].mapperMetaModelForQuery.methodName",
                invalidMethodReturnType(Long.canonicalName, String.canonicalName)),
            errorEntry("dataStorageConnectors[1].mapperMetaModelForQuery.methodName",
                invalidMethodParameter(0, BeanType.MAPPER))
        ]             | "invalid query mappers for get one object"
    }

    private static void assertFoundOneClassMetaModel(List<ClassMetaModel> classMetaModels, Predicate<ClassMetaModel> predicate) {
        def found = classMetaModels.findAll {
            predicate.test(it)
        }
        assert found.size() == 1
    }

    private static ClassMetaModelDto PERSON_METAMODEL = ClassMetaModelDto.builder()
        .name("person")
        .fields([
            createIdFieldType("id", Long),
            createValidFieldMetaModelDto("name", String),
            createValidFieldMetaModelDto("surname", String)
        ])
        .build()

    private static ClassMetaModelDto LIST_OF_PERSON_METAMODEL = createClassMetaModelDtoWithGenerics(List, PERSON_METAMODEL)

    private static ClassMetaModelDto PAGE_OF_PERSON_METAMODEL = createClassMetaModelDtoWithGenerics(Page, PERSON_METAMODEL)

    private static ClassMetaModelDto PERSON_METAMODEL_DS = ClassMetaModelDto.builder()
        .name("person_ds")
        .fields([
            createIdFieldType("id", String),
            createValidFieldMetaModelDto("name", String),
            createValidFieldMetaModelDto("surname", String),
            createValidFieldMetaModelDto("otherId", Long)
        ])
        .build()

    private static ClassMetaModelDto GENERIC_ENUM = createEnumMetaModel("VAL1", "VAL2")

    private static ClassMetaModelDto REAL_DTO = createClassMetaModelDtoFromClass(SomeRawDto)

    private static BeanAndMethodDto DEFAULT_GENERIC_SERVICE_BEAN = BeanAndMethodDto.builder()
        .className(DefaultGenericService.canonicalName)
        .methodName("saveOrReadFromDataStorages")
        .build()

    private static BeanAndMethodDto DEFAULT_GENERIC_MAPPER_BEAN = BeanAndMethodDto.builder()
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
