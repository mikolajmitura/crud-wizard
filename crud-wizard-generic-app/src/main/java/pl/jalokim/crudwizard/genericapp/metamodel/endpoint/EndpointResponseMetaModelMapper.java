package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class EndpointResponseMetaModelMapper
    extends AdditionalPropertyMapper<EndpointResponseMetaModelDto, EndpointResponseMetaModelEntity, EndpointResponseMetaModel> {

    @Autowired
    private QueryProviderMapper queryProviderMapper;

    public EndpointResponseMetaModel toEndpointResponseMetaModel(MetaModelContext metaModelContext,
        EndpointResponseMetaModelEntity endpointResponseMetaModelEntity) {
        if (endpointResponseMetaModelEntity == null) {
            return null;
        }

        return toMetaModel(endpointResponseMetaModelEntity).toBuilder()
            .classMetaModel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointResponseMetaModelEntity.getClassMetaModel().getId()))
            .mapperMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                endpointResponseMetaModelEntity::getMapperMetaModel))
                .orElse(metaModelContext.getDefaultMapperMetaModel()))
            .queryProvider(queryProviderMapper.mapInstance(endpointResponseMetaModelEntity.getQueryProvider()))
            .build();
    }

    @Override
    @Mapping(target = "classMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    @Mapping(target = "queryProvider", ignore = true)
    public abstract EndpointResponseMetaModel toMetaModel(EndpointResponseMetaModelEntity endpointResponseMetaModelEntity);
}
