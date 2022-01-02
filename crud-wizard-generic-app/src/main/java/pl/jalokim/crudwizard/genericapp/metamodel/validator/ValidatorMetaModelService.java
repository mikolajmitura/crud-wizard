package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@MetamodelService
@RequiredArgsConstructor
public class ValidatorMetaModelService {

    private final ValidatorMetaModelMapper validatorMetaModelMapper;
    private final ValidatorMetaModelRepository validatorMetaModelRepository;
    private final InstanceLoader instanceLoader;

    public List<ValidatorMetaModel> findAllMetaModels() {
        return mapToList(validatorMetaModelRepository.findAll(), validatorMetaModelMapper::toFullMetaModel);
    }

    public void saveOrCreateNewValidators(List<ValidatorMetaModelEntity> validators) {
        elements(validators)
            .forEachWithIndexed(indexed -> {
                var validatorEntry = indexed.getValue();
                validators.set(indexed.getIndex(), findOrSaveNew(validatorEntry));
            });
    }

    private ValidatorMetaModelEntity findOrSaveNew(ValidatorMetaModelEntity validatorMetaModelEntity) {
        if (validatorMetaModelEntity.getParametrized()) {
            return validatorMetaModelRepository.save(validatorMetaModelEntity);
        }

        if (validatorMetaModelEntity.getClassName() != null) {
            return validatorMetaModelRepository
                .findByClassName(validatorMetaModelEntity.getClassName())
                .orElseGet(() -> {
                    DataValidator<?> dataValidator = instanceLoader.createInstanceOrGetBean(validatorMetaModelEntity.getClassName());
                    validatorMetaModelEntity.setValidatorName(dataValidator.validatorName());
                    return validatorMetaModelRepository.save(validatorMetaModelEntity);
                });
        }

        if (validatorMetaModelEntity.getValidatorName() != null) {
            return validatorMetaModelRepository
                .findByValidatorName(validatorMetaModelEntity.getValidatorName())
                .orElseGet(() -> validatorMetaModelRepository.save(validatorMetaModelEntity));
        }
        throw new IllegalArgumentException("Cannot save validator metamodel without validator name or class name");
    }
}
