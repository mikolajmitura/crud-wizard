package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import java.util.List;
import javax.persistence.EntityManager;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;

@MetamodelService
public class ApiTagService extends BaseService<ApiTagEntity, ApiTagRepository> {

    private final ApiTagMapper apiTagMapper;

    public ApiTagService(ApiTagRepository repository, ApiTagMapper apiTagMapper, EntityManager entityManager) {
        super(repository, entityManager);
        this.apiTagMapper = apiTagMapper;
    }

    public List<ApiTagMetamodel> findAll() {
        return apiTagMapper.toDtoList(repository.findAll());
    }
}
