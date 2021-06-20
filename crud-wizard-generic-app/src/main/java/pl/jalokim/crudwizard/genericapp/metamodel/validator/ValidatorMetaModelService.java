package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@RequiredArgsConstructor
public class ValidatorMetaModelService {

    private final ValidatorMetaModelMapper validatorMetaModelMapper;
    private final ValidatorMetaModelRepository validatorMetaModelRepository;

    public List<ValidatorMetaModel> findAllMetaModels() {
        return mapToList(validatorMetaModelRepository.findAll(), validatorMetaModelMapper::toFullMetaModel);
    }
}
