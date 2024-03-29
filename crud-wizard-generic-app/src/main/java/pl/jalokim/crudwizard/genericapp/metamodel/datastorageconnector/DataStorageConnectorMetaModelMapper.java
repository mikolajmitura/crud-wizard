package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;

@Mapper(config = MapperAsSpringBeanConfig.class,
    uses = {AdditionalPropertyMapper.class,
        MapperGenerateConfigurationMapper.class,
        TranslationMapper.class,
        ClassMetaModelMapper.class
    })
public abstract class DataStorageConnectorMetaModelMapper
    implements BaseMapper<DataStorageConnectorMetaModelDto, DataStorageConnectorMetaModelEntity, DataStorageConnectorMetaModel> {

    @Autowired
    private QueryProviderMapper queryProviderMapper;

    @Override
    @Mapping(target = "dataStorageMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModelForPersist", ignore = true)
    @Mapping(target = "mapperMetaModelForQuery", ignore = true)
    @Mapping(target = "classMetaModelInDataStorage", ignore = true)
    @Mapping(target = "queryProvider", ignore = true)
    public abstract DataStorageConnectorMetaModel toMetaModel(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity);

    public DataStorageConnectorMetaModel toFullMetaModel(MetaModelContext metaModelContext,
        DataStorageConnectorMetaModelEntity dataStorageConnectorEntity) {

        return toMetaModel(dataStorageConnectorEntity).toBuilder()
            .id(dataStorageConnectorEntity.getId())
            .dataStorageMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getDataStorages,
                dataStorageConnectorEntity::getDataStorageMetaModel))
                .orElse(metaModelContext.getDefaultDataStorageMetaModel())
            )
            .mapperMetaModelForPersist(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                dataStorageConnectorEntity::getMapperMetaModelForPersist))
                .orElse(metaModelContext.getDefaultPersistMapperMetaModel())
            )
            .mapperMetaModelForQuery(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                dataStorageConnectorEntity::getMapperMetaModelForQuery))
                .orElse(metaModelContext.getDefaultQueryMapperMetaModel())
            )
            .classMetaModelInDataStorage(
                getFromContextByEntity(
                    metaModelContext::getClassMetaModels,
                    dataStorageConnectorEntity::getClassMetaModelInDataStorage
                )
            )
            .queryProvider(queryProviderMapper.mapInstance(dataStorageConnectorEntity.getQueryProvider()))
            .build();
    }

    // TODO #53 remove this after impl
    @Mapping(target = "mapperScript", ignore = true)
    @Mapping(target = "metamodelDtoType", ignore = true)
    public abstract MapperMetaModelDto toMapperMetaModelDto(MapperMetaModelEntity entity);
}
