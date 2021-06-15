package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class DataStorageMetaModelMapper extends AdditionalPropertyMapper<DataStorageMetaModel, DataStorageMetaModelEntity> {

}
