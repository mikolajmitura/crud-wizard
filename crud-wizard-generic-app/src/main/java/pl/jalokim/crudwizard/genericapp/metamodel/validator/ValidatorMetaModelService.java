package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@RequiredArgsConstructor
public class ValidatorMetaModelService {

    private final ValidatorMetaModelMapper apiTagMapper;
    private final ValidatorMetaModelRepository apiTagRepository;

    public List<ValidatorMetaModel> findAll() {
        return apiTagMapper.toDtoList(apiTagRepository.findAll());
    }
}
