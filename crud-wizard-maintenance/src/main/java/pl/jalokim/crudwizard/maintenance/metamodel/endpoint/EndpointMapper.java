package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.annotations.MapperAsSpringBeanConfig;

@Mapper(config = MapperAsSpringBeanConfig.class)
public interface EndpointMapper {

    EndpointMetaModelEntity toEntity(CreateEndpointMetaModelDto createEndpointMetaModelDto);
}
