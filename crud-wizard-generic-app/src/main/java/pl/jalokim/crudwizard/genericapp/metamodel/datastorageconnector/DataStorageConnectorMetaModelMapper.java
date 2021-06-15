package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class DataStorageConnectorMetaModelMapper extends AdditionalPropertyMapper<DataStorageConnectorMetaModel, DataStorageConnectorMetaModelEntity> {

    @Override
    @Mapping(target = "dataStorageMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    @Mapping(target = "classMetaModelInDataStorage", ignore = true)
    public abstract DataStorageConnectorMetaModel toDto(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity);

    public DataStorageConnectorMetaModel toDto(MetaModelContext metaModelContext, DataStorageConnectorMetaModelEntity dataStorageConnectorEntity) {
        return DataStorageConnectorMetaModel.builder()
            .dataStorageMetaModel(
                getFromContextByEntity(
                    metaModelContext::getDataStorages,
                    dataStorageConnectorEntity::getDataStorageMetaModel)
            )
            .mapperMetaModel(
                getFromContextByEntity(
                    metaModelContext::getMapperMetaModels,
                    dataStorageConnectorEntity::getMapperMetaModel)
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
