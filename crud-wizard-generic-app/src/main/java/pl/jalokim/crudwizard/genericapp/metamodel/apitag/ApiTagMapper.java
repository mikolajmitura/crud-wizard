package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.metamodels.ApiTagMetamodel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public interface ApiTagMapper extends BaseMapper<ApiTagMetamodel, ApiTagEntity, ApiTagMetamodel> {

}
