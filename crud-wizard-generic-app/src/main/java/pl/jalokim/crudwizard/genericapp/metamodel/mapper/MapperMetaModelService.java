package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import java.util.Optional;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;

@MetamodelService
public class MapperMetaModelService extends BaseService<MapperMetaModelEntity, MapperMetaModelEntityRepository> {

    private final MapperMetaModelMapper mapperMetaModelMapper;
    private final ClassMetaModelService classMetaModelService;

    public MapperMetaModelService(MapperMetaModelEntityRepository repository,
        MapperMetaModelMapper mapperMetaModelMapper, ClassMetaModelService classMetaModelService) {
        super(repository);
        this.mapperMetaModelMapper = mapperMetaModelMapper;
        this.classMetaModelService = classMetaModelService;
    }

    public List<MapperMetaModel> findAllMetaModels() {
        return mapToList(repository.findAll(), mapperMetaModelMapper::toFullMetaModel);
    }

    public boolean exists(MapperMetaModelDto mapperMetaModelDto) {
        BeanAndMethodDto mapperBeanAndMethod = mapperMetaModelDto.getMapperBeanAndMethod();
        return repository.existsByBeanNameAndClassNameAndMethodName(mapperBeanAndMethod.getBeanName(),
            mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getMethodName());
    }

    public Long createNewAndGetId(MapperMetaModelDto mapperMetaModelDto) {
        return save(mapperMetaModelMapper.toEntity(mapperMetaModelDto))
            .getId();
    }

    @Override
    public MapperMetaModelEntity save(MapperMetaModelEntity mapperMetaModelEntity) {
        Optional.ofNullable(mapperMetaModelEntity.getMapperGenerateConfiguration())
            .map(MapperGenerateConfigurationEntity::getRootConfiguration)
            .ifPresent(this::saveMapperConfigurationEntity);

        Optional.ofNullable(mapperMetaModelEntity.getMapperGenerateConfiguration())
            .map(MapperGenerateConfigurationEntity::getSubMappersAsMethods)
            .ifPresent(subMappers -> subMappers.forEach(this::saveMapperConfigurationEntity));

        return super.save(mapperMetaModelEntity);
    }

    private void saveMapperConfigurationEntity(MapperConfigurationEntity mapperConfigurationEntity) {
        mapperConfigurationEntity.setSourceMetaModel(classMetaModelService.saveNewOrLoadById(mapperConfigurationEntity.getSourceMetaModel()));
        mapperConfigurationEntity.setTargetMetaModel(classMetaModelService.saveNewOrLoadById(mapperConfigurationEntity.getTargetMetaModel()));
    }
}
