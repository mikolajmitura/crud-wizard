package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import java.util.List;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ApiTagDto;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;

@MetamodelService
@RequiredArgsConstructor
public class ApiTagService {

    private final ApiTagMapper apiTagMapper;
    private final ApiTagRepository apiTagRepository;

    public List<ApiTagDto> findAll() {
        return apiTagMapper.toDtoList(apiTagRepository.findAll());
    }
}
