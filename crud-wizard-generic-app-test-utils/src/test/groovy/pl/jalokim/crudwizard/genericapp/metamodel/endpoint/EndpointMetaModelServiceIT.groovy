package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty
import static pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel.PLACEHOLDER_PREFIX
import static pl.jalokim.crudwizard.core.rest.response.error.ErrorDto.errorEntry
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.emptyEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService.createNewEndpointReason
import static pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModelDtoSamples.createAdditionalValidatorsForExtendedPerson
import static pl.jalokim.crudwizard.test.utils.validation.ValidationErrorsAssertion.assertValidationResults
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import javax.validation.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.genericapp.customendpoint.SomeCustomRestController
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.context.ContextRefreshStatus
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshRepository
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation.EndpointNotExistsAlready
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorInstanceCache
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelRepository
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
    private ValidatorInstanceCache validatorInstanceCache

    @Autowired
    private ValidatorMetaModelRepository validatorMetaModelRepository

    def "should save POST new endpoint with default mapper, service, data storage"() {
        given:
        def validatorInstancesCache = validatorInstanceCache.dataValidatorsByKey
        validatorInstancesCache.clear()
        def createEndpointMetaModelDto = createValidPostEndpointMetaModelDto().toBuilder()
            .payloadMetamodel(extendedPersonClassMetaModel())
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
        validatorInstancesCache.size() == 2
        validatorInstancesCache.get(NotNullValidator.canonicalName) != null
        validatorInstancesCache.get(SizeValidator.canonicalName) != null
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
            it.name == PLACEHOLDER_PREFIX + name && it.jsonValue == value.toString()
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

    // TODO save all fields during create endpoint metamodels, queryArguments, responseMetaModel with not raw java class but with class metadata,
    //  classMetaModelInDataStorage other than payload

    // TODO new endpoint with already existed metamodels
    // TODO save with new service
    // TODO save with new mapper
    // TODO save with not default data storage
}
