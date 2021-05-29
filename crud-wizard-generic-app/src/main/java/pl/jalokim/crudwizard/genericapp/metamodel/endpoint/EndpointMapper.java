package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public interface EndpointMapper {

    EndpointMetaModelEntity toEntity(CreateEndpointMetaModelDto createEndpointMetaModelDto);
}
