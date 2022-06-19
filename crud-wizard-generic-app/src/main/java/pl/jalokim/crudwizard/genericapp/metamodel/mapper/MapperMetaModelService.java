package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;

@MetamodelService
public class MapperMetaModelService extends BaseService<MapperMetaModelEntity, MapperMetaModelEntityRepository> {

    private final MapperMetaModelMapper mapperMetaModelMapper;

    public MapperMetaModelService(MapperMetaModelEntityRepository repository,
        MapperMetaModelMapper mapperMetaModelMapper) {
        super(repository);
        this.mapperMetaModelMapper = mapperMetaModelMapper;
    }

    public List<MapperMetaModel> findAllMetaModels() {
        return mapToList(repository.findAll(), mapperMetaModelMapper::toFullMetaModel);
    }

    public boolean exists(MapperMetaModelDto mapperMetaModelDto) {
        return repository.existsByBeanNameAndClassNameAndMethodName(mapperMetaModelDto.getBeanName(),
            mapperMetaModelDto.getClassName(), mapperMetaModelDto.getMethodName());
    }

    public Long createNewAndGetId(MapperMetaModelDto mapperMetaModelDto) {
        return repository.save(mapperMetaModelMapper.toEntity(mapperMetaModelDto))
            .getId();
    }
}
