package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity;
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

        return toModel(endpointResponseMetaModelEntity).toBuilder()
            .classMetaModel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointResponseMetaModelEntity.getClassMetaModel().getId()))
            .mapperMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                endpointResponseMetaModelEntity::getMapperMetaModel))
                .orElse(metaModelContext.getDefaultMapperMetaModel()))
            .queryProvider(queryProviderMapper.mapInstance(metaModelContext,
                endpointResponseMetaModelEntity.getQueryProvider()))
            .build();
    }

    @Mapping(target = "classMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    @Mapping(target = "queryProvider", ignore = true)
    public abstract EndpointResponseMetaModel toModel(EndpointResponseMetaModelEntity endpointResponseMetaModelEntity);

    @Mapping(target = "queryProvider", ignore = true)
    public abstract DataStorageConnectorMetaModel toMetaModel(DataStorageConnectorMetaModelEntity dataStorageConnectorMetaModelEntity);
}
