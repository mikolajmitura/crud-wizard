package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

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
        if (endpointResponseMetaModelEntity == null) {
            return null;
        }

        return toModel(endpointResponseMetaModelEntity).toBuilder()
            .classMetaModel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointResponseMetaModelEntity.getClassMetaModel().getId()))
            .mapperMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                endpointResponseMetaModelEntity::getMapperMetaModel))
                .orElse(metaModelContext.getDefaultMapperMetaModel()))
            .build();
    }

    @Mapping(target = "classMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    public abstract EndpointResponseMetaModel toModel(EndpointResponseMetaModelEntity endpointResponseMetaModelEntity);
}
