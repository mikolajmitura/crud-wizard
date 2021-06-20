package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ApiTagMetamodel;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@RequiredArgsConstructor
public class ApiTagService {

    private final ApiTagMapper apiTagMapper;
    private final ApiTagRepository apiTagRepository;

    public List<ApiTagMetamodel> findAll() {
        return apiTagMapper.toDtoList(apiTagRepository.findAll());
    }
}
