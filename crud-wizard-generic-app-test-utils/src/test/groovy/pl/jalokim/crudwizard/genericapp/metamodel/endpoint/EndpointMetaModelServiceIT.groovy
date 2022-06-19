package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel1
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel.PLACEHOLDER_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDtoSamples.createSampleDataStorageConnectorDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators2
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService.createNewEndpointReason
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDtoSamples.createValidValidatorMetaModelDto
import static pl.jalokim.crudwizard.test.utils.translations.AppMessageSourceTestImpl.messageForValidator
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import java.util.function.Predicate
import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue
import pl.jalokim.crudwizard.genericapp.customendpoint.SomeCustomRestController
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.context.ContextRefreshStatus
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshRepository
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDtoSamples
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.validation.VerifyThatCanCreateDataStorage
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelRepository
import pl.jalokim.crudwizard.genericapp.rest.samples.datastorage.DataStorageWithoutFactory
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator
import pl.jalokim.crudwizard.test.utils.validation.ValidatorWithConverter

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

                def foundDocumentEntity = foundDocumentsEntity.fieldType.genericTypes[0]

                def foundValueFieldEntity = foundDocumentEntity.fields.find {
                    it.fieldName == "value"
                }
                foundDocumentEntity.validators == [notNullValidator]
                foundValueFieldEntity.validators == [notNullValidator, documentValueSizeValidator]

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

    def "return exception when class meta model DTOs are not the same with the same name"() {
        given:
        def person1 = simplePersonClassMetaModel().toBuilder()
            .validators([createValidValidatorMetaModelDto()])
            .build()
        def person2 = simplePersonClassMetaModel()
        def someObject = ClassMetaModelDto.builder()
            .name("someObject")
            .isGenericEnumType(false)
            .fields([
                createValidFieldMetaModelDto("person1", person1),
                createValidFieldMetaModelDto("person2", person2),
            ])
            .build()

        def createPostPersonEndpoint = createValidPostExtendedUserWithValidators2().toBuilder()
            .payloadMetamodel(someObject)
            .build()

        when:
        endpointMetaModelService.createNewEndpoint(createPostPersonEndpoint)

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("class.metamodel.the.same.name.but.other.content", "simple-person")
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
                classMetaModel -> classMetaModel.name == "simple-person"})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.name == "document"})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == List &&
                    classMetaModel.genericTypes.size() == 1 &&
                    classMetaModel.genericTypes.find { it.name == "document"} != null
            })
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.name == "exampleEnum" && classMetaModel.isGenericMetamodelEnum()})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == Long && classMetaModel.simpleRawClass})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == Byte && classMetaModel.simpleRawClass})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == String && classMetaModel.simpleRawClass})
            assertFoundOneClassMetaModel(classMetaModels, {
                classMetaModel -> classMetaModel.realClass == LocalDate && classMetaModel.simpleRawClass})
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

    // TODO #1 #tempoaray_context_metamodels test with first validation phase via TemporaryContextLoader not passed

    // TODO #1 #tempoaray_context_metamodels
    //  test when create person which contains fields:
    //  List<person> children
    //  List<document> documents (with real definition)
    //  List<document> expiredDocuments (with definition by name)
    //  Set<invoice> invoices (with definition by id)
    //  Set<Agreement> oldAgreements (with real class)
    //  Agreement currentAgreement (with real class)

    private static void assertFoundOneClassMetaModel(List<ClassMetaModel> classMetaModels, Predicate<ClassMetaModel> predicate) {
        def found = classMetaModels.findAll {
            predicate.test(it)
        }
        assert found.size() == 1
    }
}
