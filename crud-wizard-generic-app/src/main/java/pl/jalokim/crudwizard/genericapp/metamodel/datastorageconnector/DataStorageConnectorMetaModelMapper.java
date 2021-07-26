package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class DataStorageConnectorMetaModelMapper
    extends AdditionalPropertyMapper<DataStorageConnectorMetaModelDto, DataStorageConnectorMetaModelEntity, DataStorageConnectorMetaModel> {

    @Override
    @Mapping(target = "dataStorageMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    @Mapping(target = "classMetaModelInDataStorage", ignore = true)
    public abstract DataStorageConnectorMetaModel toMetaModel(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity);

    public DataStorageConnectorMetaModel toFullMetaModel(MetaModelContext metaModelContext, DataStorageConnectorMetaModelEntity dataStorageConnectorEntity) {
        return DataStorageConnectorMetaModel.builder()
            .id(dataStorageConnectorEntity.getId())
            .dataStorageMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getDataStorages,
                dataStorageConnectorEntity::getDataStorageMetaModel))
                .orElse(metaModelContext.getDefaultDataStorageMetaModel())
            )
            .mapperMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                dataStorageConnectorEntity::getMapperMetaModel))
                .orElse(metaModelContext.getDefaultMapperMetaModel())
            )
            .classMetaModelInDataStorage(
                getFromContextByEntity(
                    metaModelContext::getClassMetaModels,
                    dataStorageConnectorEntity::getClassMetaModelInDataStorage
                )
            )
            .build();
    }
}
