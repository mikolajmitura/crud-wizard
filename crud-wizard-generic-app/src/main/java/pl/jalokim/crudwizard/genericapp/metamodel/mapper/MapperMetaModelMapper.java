package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class MapperMetaModelMapper extends AdditionalPropertyMapper<MapperMetaModel, MapperMetaModelEntity> {

}
