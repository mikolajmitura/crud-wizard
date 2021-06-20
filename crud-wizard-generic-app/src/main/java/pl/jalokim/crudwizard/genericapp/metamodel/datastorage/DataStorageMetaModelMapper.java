package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import java.util.List;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.utils.collection.Elements;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class DataStorageMetaModelMapper
    extends AdditionalPropertyMapper<DataStorageMetaModelDto, DataStorageMetaModelEntity, DataStorageMetaModel> {

    @Autowired
    private List<DataStorage> dataStorages;

    public DataStorageMetaModel toFullMetaModel(DataStorageMetaModelEntity entity) {
        return toMetaModel(entity)
            .toBuilder()
            .dataStorage(
                Elements.elements(dataStorages)
                    .filter(dataStorage -> dataStorage.getClassName().equals(entity.getClassName())
                        && dataStorage.getName().equals(entity.getName()))
                    .getFirst()
            )
            .build();
    }
}
