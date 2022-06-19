package pl.jalokim.crudwizard.genericapp.metamodel.endpoint

import static pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath.ALL_INDEXES

import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModelSamples
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity
import spock.lang.Specification

class EndpointMetaModelMapperTest extends Specification {

    def "correctly map from list of AdditionalValidatorPathPairEntity to AdditionalValidatorsMetaModel"() {
        given:
        MetaModelContext metaModelContext = new MetaModelContext()
        def validatorMetaModels = new ModelsCache<ValidatorMetaModel>()
        metaModelContext.setValidatorMetaModels(validatorMetaModels)
        validatorMetaModels.put(1L, ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL)
        validatorMetaModels.put(2L, ValidatorMetaModelSamples.SIZE_2_30_VALIDATOR_METAMODEL)
        validatorMetaModels.put(3L, ValidatorMetaModelSamples.SIZE_1_MAX_VALIDATOR_METAMODEL)
        List<AdditionalValidatorsEntity> additionalValidatorsByPaths = [
            AdditionalValidatorsEntity.builder()
                .fullPropertyPath("persons")
                .validators([createValidatorMetaModelEntity(3)])
                .build(),
            AdditionalValidatorsEntity.builder()
                .fullPropertyPath("persons[*]")
                .validators([createValidatorMetaModelEntity(1)])
                .build(),
            AdditionalValidatorsEntity.builder()
                .fullPropertyPath("persons[*].name")
                .validators([createValidatorMetaModelEntity(1), createValidatorMetaModelEntity(2)])
                .build(),
            AdditionalValidatorsEntity.builder()
                .fullPropertyPath("persons[*].surname")
                .validators([createValidatorMetaModelEntity(1), createValidatorMetaModelEntity(3)])
                .build(),
            AdditionalValidatorsEntity.builder()
                .fullPropertyPath("documents")
                .validators([createValidatorMetaModelEntity(3)])
                .build(),
        ]

        when:
        def additionalValidatorsModel = EndpointMetaModelMapper.createAdditionalValidatorsMetaModel(metaModelContext, additionalValidatorsByPaths)

        then:
        verifyAll(additionalValidatorsModel) {
            validatorsMetaModel.isEmpty()
            propertyPath.getPathValue() == ""
            validatorsByPropertyPath.size() == 2
        }

        def personsNode = additionalValidatorsModel.getValidatorsByPropertyPath().get("persons")
        verifyAll(personsNode) {
            validatorsMetaModel == [ValidatorMetaModelSamples.SIZE_1_MAX_VALIDATOR_METAMODEL]
            propertyPath.getPathValue() == "persons"
            validatorsByPropertyPath.size() == 1
        }

        def personsAllIndexesNode = personsNode.getValidatorsByPropertyPath().get(ALL_INDEXES)
        verifyAll(personsAllIndexesNode) {
            validatorsMetaModel == [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL]
            propertyPath.getPathValue() == ALL_INDEXES
            propertyPath.buildFullPath() == "persons[*]"
            validatorsByPropertyPath.size() == 2
        }

        def personNameNode = personsAllIndexesNode.getValidatorsByPropertyPath().get("surname")
        verifyAll(personNameNode) {
            validatorsMetaModel == [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL, ValidatorMetaModelSamples.SIZE_1_MAX_VALIDATOR_METAMODEL]
            propertyPath.getPathValue() == "surname"
            propertyPath.buildFullPath() == "persons[*].surname"
            validatorsByPropertyPath.isEmpty()
        }

        def personSurnameNode = personsAllIndexesNode.getValidatorsByPropertyPath().get("name")
        verifyAll(personSurnameNode) {
            validatorsMetaModel == [ValidatorMetaModelSamples.NOT_NULL_VALIDATOR_METAMODEL, ValidatorMetaModelSamples.SIZE_2_30_VALIDATOR_METAMODEL]
            propertyPath.getPathValue() == "name"
            propertyPath.buildFullPath() == "persons[*].name"
            validatorsByPropertyPath.isEmpty()
        }

        def documentsNode = additionalValidatorsModel.getValidatorsByPropertyPath().get("documents")
        verifyAll(documentsNode) {
            validatorsMetaModel == [ValidatorMetaModelSamples.SIZE_1_MAX_VALIDATOR_METAMODEL]
            propertyPath.getPathValue() == "documents"
            propertyPath.buildFullPath() == "documents"
            validatorsByPropertyPath.isEmpty()
        }
    }

    ValidatorMetaModelEntity createValidatorMetaModelEntity(Long id) {
        ValidatorMetaModelEntity.builder()
            .id(id)
            .build()
    }
}
