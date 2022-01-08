package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class DataStorageMetaModelMapper
    extends AdditionalPropertyMapper<DataStorageMetaModelDto, DataStorageMetaModelEntity, DataStorageMetaModel> {

    @Autowired
    private DataStorageInstances dataStorageInstances;

    @Override
    @Mapping(target = "dataStorage", ignore = true)
    public abstract DataStorageMetaModel toMetaModel(DataStorageMetaModelEntity entity);

    public DataStorageMetaModel toFullMetaModel(DataStorageMetaModelEntity entity) {
        return toMetaModel(entity)
            .toBuilder()
            .dataStorage(dataStorageInstances.findDataStorageOrCreate(entity))
            .build();
    }
}
