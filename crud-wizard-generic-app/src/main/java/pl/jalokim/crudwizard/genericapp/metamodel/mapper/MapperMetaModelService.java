package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import java.util.List;
import java.util.Optional;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseService;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGeneratedInstanceService;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodDto;

@MetamodelService
public class MapperMetaModelService extends BaseService<MapperMetaModelEntity, MapperMetaModelEntityRepository> {

    private final MapperMetaModelMapper mapperMetaModelMapper;
    private final ClassMetaModelService classMetaModelService;
    private final MapperGeneratedInstanceService mapperGeneratedInstanceService;

    public MapperMetaModelService(MapperMetaModelEntityRepository repository,
        MapperMetaModelMapper mapperMetaModelMapper, ClassMetaModelService classMetaModelService,
        MapperGeneratedInstanceService mapperGeneratedInstanceService) {
        super(repository);
        this.mapperMetaModelMapper = mapperMetaModelMapper;
        this.classMetaModelService = classMetaModelService;
        this.mapperGeneratedInstanceService = mapperGeneratedInstanceService;
    }

    public List<MapperMetaModel> findAllMetaModels(MetaModelContext metaModelContext) {
        return mapToList(repository.findAll(),
            metaModelEntity -> mapperMetaModelMapper.toFullMetaModel(metaModelEntity, metaModelContext));
    }

    public void updateGeneratedMappers(MetaModelContext metaModelContext) {
        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(metaModelContext, null);
        TemporaryModelContextHolder.setTemporaryContext(temporaryMetaModelContext);
        repository.findAllByMapperType(MapperType.GENERATED).forEach(mapperMetaModelEntity -> {
            MapperGenerateConfigurationEntity mapperGenerateConfiguration = mapperMetaModelEntity.getMapperGenerateConfiguration();
            MapperMetaModel mapperMetaModel = metaModelContext.findMapperMetaModelByName(mapperMetaModelEntity.getMapperName());
            mapperMetaModel.setMapperInstance(mapperGeneratedInstanceService.loadMapperInstanceOrGenerateNew(mapperGenerateConfiguration, metaModelContext));
        });
        TemporaryModelContextHolder.clearTemporaryMetaModelContext();
    }

    public Long findIdForMapperModel(MapperMetaModelDto mapperMetaModelDto) {
        BeanAndMethodDto mapperBeanAndMethod = mapperMetaModelDto.getMapperBeanAndMethod();
        return repository.findByBeanNameAndClassNameAndMethodName(mapperBeanAndMethod.getBeanName(),
            mapperBeanAndMethod.getClassName(), mapperBeanAndMethod.getMethodName())
            .map(MapperMetaModelEntity::getId)
            .orElse(null);
    }

    public Long createNewAndGetId(MapperMetaModelDto mapperMetaModelDto) {
        return save(mapperMetaModelMapper.toEntity(mapperMetaModelDto))
            .getId();
    }

    @Override
    public MapperMetaModelEntity save(MapperMetaModelEntity mapperMetaModelEntity) {

        MapperGenerateConfigurationEntity mapperGenerateConfiguration = mapperMetaModelEntity.getMapperGenerateConfiguration();
        if (mapperGenerateConfiguration != null) {
            Optional.ofNullable(mapperGenerateConfiguration.getRootConfiguration())
                .ifPresent(this::saveMapperConfigurationEntity);

            Optional.ofNullable(mapperGenerateConfiguration.getSubMappersAsMethods())
                .ifPresent(subMappers -> subMappers.forEach(this::saveMapperConfigurationEntity));

            mapperGenerateConfiguration.setPathVariablesClassModel(
                classMetaModelService.saveNewOrLoadById(mapperGenerateConfiguration.getPathVariablesClassModel()));
            mapperGenerateConfiguration.setRequestParamsClassModel(
                classMetaModelService.saveNewOrLoadById(mapperGenerateConfiguration.getRequestParamsClassModel()));
        }

        return super.save(mapperMetaModelEntity);
    }

    private void saveMapperConfigurationEntity(MapperConfigurationEntity mapperConfigurationEntity) {
        mapperConfigurationEntity.setSourceMetaModel(classMetaModelService.saveNewOrLoadById(mapperConfigurationEntity.getSourceMetaModel()));
        mapperConfigurationEntity.setTargetMetaModel(classMetaModelService.saveNewOrLoadById(mapperConfigurationEntity.getTargetMetaModel()));
    }
}
