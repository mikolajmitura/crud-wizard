package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class EndpointResponseMetaModelMapper
    extends AdditionalPropertyMapper<EndpointResponseMetaModelDto, EndpointResponseMetaModelEntity, EndpointResponseMetaModel> {

    public EndpointResponseMetaModel toEndpointResponseMetaModel(MetaModelContext metaModelContext,
        EndpointResponseMetaModelEntity endpointResponseMetaModelEntity) {

        return toModel(endpointResponseMetaModelEntity).toBuilder()
            .classMetaModel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointResponseMetaModelEntity.getClassMetaModel().getId()))
            .build();
    }

    @Mapping(target = "classMetaModel", ignore = true)
    public abstract EndpointResponseMetaModel toModel(EndpointResponseMetaModelEntity endpointResponseMetaModelEntity);
}