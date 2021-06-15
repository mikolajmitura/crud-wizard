package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getListFromContext;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModelDto;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class EndpointMetaModelMapper extends AdditionalPropertyMapper<EndpointMetaModelDto, EndpointMetaModelEntity> {

    @Autowired
    private EndpointResponseMetaModelMapper endpointResponseMetaModelMapper;

    @Override
    public abstract EndpointMetaModelEntity toEntity(EndpointMetaModelDto createEndpointMetaModelDto);

    @Override
    @Mapping(target = "apiTag", ignore = true)
    @Mapping(target = "payloadMetamodel", ignore = true)
    @Mapping(target = "queryArguments", ignore = true)
    @Mapping(target = "serviceMetaModel", ignore = true)
    @Mapping(target = "responseMetaModel", ignore = true)
    public abstract EndpointMetaModelDto toDto(EndpointMetaModelEntity endpointMetaModelEntity);

    public EndpointMetaModelDto toDto(MetaModelContext metaModelContext, EndpointMetaModelEntity endpointMetaModelEntity) {
        EndpointMetaModelDto endpointMetaModelDto = toDto(endpointMetaModelEntity);
        return endpointMetaModelDto.toBuilder()
            .apiTag(getFromContext(metaModelContext::getApiTags, () -> endpointMetaModelEntity.getApiTag().getId()))
            .payloadMetamodel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointMetaModelEntity.getPayloadMetamodel().getId()))
            .queryArguments(getListFromContext(
                endpointMetaModelEntity.getQueryArguments(), metaModelContext::getClassMetaModels, ClassMetaModelEntity::getId))
            .serviceMetaModel(getFromContext(metaModelContext::getServiceMetaModels, () -> endpointMetaModelEntity.getServiceMetaModel().getId()))
            .responseMetaModel(endpointResponseMetaModelMapper.toEndpointResponseMetaModel(metaModelContext, endpointMetaModelEntity.getResponseMetaModel()))
            .build();
    }

}
