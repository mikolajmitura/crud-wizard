package pl.jalokim.crudwizard.genericapp.metamodel.context

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel.PLACEHOLDER_PREFIX
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostExtendedUserWithValidators

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage
import pl.jalokim.crudwizard.genericapp.mapper.DefaultGenericMapper
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagSamples
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagService
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModel
import pl.jalokim.crudwizard.genericapp.provider.DefaultBeansConfigService
import pl.jalokim.crudwizard.genericapp.service.DefaultGenericService
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader
import pl.jalokim.crudwizard.genericapp.validation.validator.NotNullValidator
import pl.jalokim.crudwizard.genericapp.validation.validator.SizeValidator

class MetaModelContextServiceIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private ApiTagSamples apiTagSamples

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    @Autowired
    private DataStorageMetaModelService dataStorageMetaModelService

    @Autowired
    private ApiTagService apiTagService

    @Autowired
    private MapperMetaModelService mapperMetaModelService

    @Autowired
    private ServiceMetaModelService serviceMetaModelService

    @Autowired
    private DefaultBeansConfigService defaultBeansService

    @Autowired
    private InMemoryDataStorage inMemoryDataStorage

    @Autowired
    private DefaultGenericMapper genericMapperBean

    @Autowired
    private DefaultGenericService genericServiceBean

    @Autowired
    private InstanceLoader instanceLoader

    /*
    test for create new endpoint which doesn't use currently created meta model. It creates all of them.
     */

    def "should load all meta models as expected for one endpoint with default mapper, default service, default data storage"() {
        given:
        def firstApiTag = apiTagSamples.saveNewApiTag()
        def secondApiTag = apiTagSamples.saveNewApiTag()
        def endpointMetaModelDto = createValidPostExtendedUserWithValidators()

        def endpointId = endpointMetaModelService.createNewEndpoint(endpointMetaModelDto)

        when:
        metaModelContextService.reloadAll()
        def reloadedContext = metaModelContextService.getMetaModelContext()

        then:
        verifyAll(reloadedContext) {
            dataStorages.objectsById == dataStorageMetaModelService.findAllMetaModels()
                .collectEntries {
                    [it.id, it]
                }
            verifyAll(defaultDataStorageMetaModel) {
                id == defaultBeansService.getDefaultDataStorageId()
                name == InMemoryDataStorage.DEFAULT_DS_NAME
                dataStorage == inMemoryDataStorage
            }

            apiTags.objectsById == apiTagService.findAll()
                .collectEntries {
                    [it.id, it]
                }
            apiTags.objectsById.values()*.name as Set == [firstApiTag, secondApiTag, endpointMetaModelDto.apiTag]*.name as Set

            validatorMetaModels.objectsById.size() == 5

            /* assert additional validators */
            def allValidators = validatorMetaModels.objectsById.values()
            def notNullValidatorModel = allValidators.find { it.realClass == NotNullValidator}
            def documentValueSizeValidatorModel = findSizeValidator(allValidators, 5, 25)
            def additionalPersonNameSizeValidatorModel = findSizeValidator(allValidators, 2, 20)
            def additionalPersonSurnameSizeValidatorModel = findSizeValidator(allValidators, 2, 30)
            def additionalDocumentsSizeValidatorModel = findSizeValidator(allValidators, 1, null)

            def notNullValidator = instanceLoader.createInstanceOrGetBean(NotNullValidator.canonicalName)
            def sizeValidator = instanceLoader.createInstanceOrGetBean(SizeValidator.canonicalName)
            notNullValidator == notNullValidatorModel.validatorInstance
            sizeValidator == documentValueSizeValidatorModel.validatorInstance
            sizeValidator == additionalPersonNameSizeValidatorModel.validatorInstance
            sizeValidator == additionalPersonSurnameSizeValidatorModel.validatorInstance
            sizeValidator == additionalDocumentsSizeValidatorModel.validatorInstance

            def payloadMetaModelInstance = classMetaModels.objectsById.values().find {
                it.name == endpointMetaModelDto.payloadMetamodel.name
            }

            /* assert validators in fields and classes meta models */
            def documentsMetaModel = payloadMetaModelInstance.getFieldByName("documents").fieldType
            def documentMetaModel = documentsMetaModel.genericTypes[0]
            def documentValueFieldMetaModel = documentMetaModel.getFieldByName("value")
            documentMetaModel.validators == [notNullValidatorModel]
            documentValueFieldMetaModel.validators == [notNullValidatorModel, documentValueSizeValidatorModel]

            def stringClassMetaModel = findClassMetaModel(classMetaModels, String)
            def longClassMetaModel = findClassMetaModel(classMetaModels, Long)

            payloadMetaModelInstance.getFieldByName("id").fieldType == longClassMetaModel
            payloadMetaModelInstance.getFieldByName("name").fieldType == stringClassMetaModel
            payloadMetaModelInstance.getFieldByName("surname").fieldType == stringClassMetaModel
            documentMetaModel.getFieldByName("id").fieldType == longClassMetaModel
            documentMetaModel.getFieldByName("value").fieldType == stringClassMetaModel

            assertClassMetaModels(payloadMetaModelInstance, endpointMetaModelDto.payloadMetamodel)

            verifyAll(classMetaModels.objectsById.values().find {
                it.className == String.canonicalName
            }) {
                realClass == String
            }

            def apiResponseMetaModel = classMetaModels.objectsById.values().find {
                it.className == Long.canonicalName
            }
            verifyAll(apiResponseMetaModel) {
                realClass == Long
            }

            mapperMetaModels.objectsById.values()*.className as Set == mapperMetaModelService.findAllMetaModels()*.className as Set
            verifyAll(defaultMapperMetaModel) {
                id == defaultBeansService.getDefaultGenericMapperId()
                mapperInstance == genericMapperBean
                className == DefaultGenericMapper.canonicalName
                beanName == "defaultGenericMapper"
                methodName == "mapToTarget"
                methodMetaModel.name == "mapToTarget"
            }

            serviceMetaModels.objectsById.values()*.className as Set == serviceMetaModelService.findAllMetaModels()*.className as Set
            verifyAll(defaultServiceMetaModel) {
                id == defaultBeansService.getDefaultGenericServiceId()
                serviceInstance == genericServiceBean
                className == DefaultGenericService.canonicalName
                beanName == "defaultGenericService"
                methodName == "saveOrReadFromDataStorages"
                methodMetaModel.name == "saveOrReadFromDataStorages"
                serviceScript == null
            }

            defaultDataStorageConnectorMetaModels*.id as Set == defaultBeansService.getDefaultDataStorageConnectorsId() as Set
            verifyAll(defaultDataStorageConnectorMetaModels[0]) {
                dataStorageMetaModel == defaultDataStorageMetaModel
                mapperMetaModelForQuery == defaultMapperMetaModel
                mapperMetaModelForReturn == defaultMapperMetaModel
                classMetaModelInDataStorage == null
            }

            verifyAll(endpointMetaModels.objectsById.get(endpointId)) {
                id == endpointId
                apiTag.id != null
                apiTag.name == endpointMetaModelDto.apiTag.name
                urlMetamodel.rawUrl == endpointMetaModelDto.baseUrl
                httpMethod == endpointMetaModelDto.httpMethod
                operationName == endpointMetaModelDto.operationName
                payloadMetamodel == payloadMetaModelInstance

                assertAdditionalValidators(payloadMetamodelAdditionalValidators.validatorsByPropertyPath["name"],
                    notNullValidatorModel, additionalPersonNameSizeValidatorModel)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators.validatorsByPropertyPath["surname"],
                    notNullValidatorModel, additionalPersonSurnameSizeValidatorModel)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators.validatorsByPropertyPath["documents"],
                    additionalDocumentsSizeValidatorModel)
                assertAdditionalValidators(payloadMetamodelAdditionalValidators.validatorsByPropertyPath["documents"]
                    .validatorsByPropertyPath["[*]"].validatorsByPropertyPath["type"], notNullValidatorModel)

                serviceMetaModel == defaultServiceMetaModel
                verifyAll(responseMetaModel) {
                    id != null
                    classMetaModel == apiResponseMetaModel
                    successHttpCode == endpointMetaModelDto.responseMetaModel.successHttpCode
                    dataStorageConnectors == defaultDataStorageConnectorMetaModels
                }
            }

            verifyAll(endpointMetaModelContextNode.nextNodesByPath[endpointMetaModelDto.baseUrl]) {
                urlPartMetaModel.urlPart.originalValue == endpointMetaModelDto.baseUrl
                endpointsByHttpMethod[endpointMetaModelDto.httpMethod] == endpointMetaModels.objectsById.get(endpointId)
            }
        }
    }

    private boolean assertClassMetaModels(ClassMetaModel classMetaModel, ClassMetaModelDto classMetaModelDto) {
        verifyAll(classMetaModel) {
            name == classMetaModelDto.name
            className == classMetaModelDto.className

            List<FieldMetaModel> fieldsModels = Optional.ofNullable(fields).orElse([])
            List<FieldMetaModelDto> fieldsDto = Optional.ofNullable(classMetaModelDto.fields).orElse([])

            fieldsModels.size() == fieldsDto.size()
            if (fieldsModels.size() > 0) {
                fieldsModels.eachWithIndex {fieldEntity, index ->
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

    private ValidatorMetaModel findSizeValidator(Collection<ValidatorMetaModel> allValidators, Long min, Long max) {
        allValidators.find {
            it.realClass == SizeValidator &&
                (min == null || foundValidatorPlaceholder(it.additionalProperties, "min", min)) &&
                (max == null || foundValidatorPlaceholder(it.additionalProperties, "max", max))
        }
    }

    private static boolean foundValidatorPlaceholder(List<AdditionalPropertyMetaModel> additionalProperties, String name, Long value) {
        additionalProperties.find {
            it.name == PLACEHOLDER_PREFIX + name && it.valueAsObject == value
        } != null
    }

    private static boolean assertAdditionalValidators(AdditionalValidatorsMetaModel payloadMetamodelAdditionalValidators,
        ValidatorMetaModel... expectedValidators) {
        payloadMetamodelAdditionalValidators.validatorsMetaModel as Set == expectedValidators as Set
    }

    private ClassMetaModel findClassMetaModel(ModelsCache<ClassMetaModel> classMetaModels, Class<?> classToFind) {
        def foundClassMetaModel = classMetaModels.objectsById.values().find {
            it.realClass == classToFind && it.simpleRawClass
        }
        verifyAll(foundClassMetaModel) {
            realClass ==  classToFind
            simpleRawClass
            validators.isEmpty()
            genericTypes.isEmpty()
            extendsFromModels.isEmpty()
            fields.isEmpty()
        }
        foundClassMetaModel
    }

    // TODO  to implement load context with custom service, mapper, data storage connectors, should verify only them not all objectsById fields.ApiTagSamples
}
