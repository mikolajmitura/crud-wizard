package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.mapper.BaseMapper;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public interface ApiTagMapper extends BaseMapper<ApiTagDto, ApiTagEntity> {

}
