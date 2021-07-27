package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.url.BaseUrlModelResolver;
import pl.jalokim.utils.collection.CollectionUtils;

// TODO try use uses to inject others mapper, now is problem with ambiguity from AdditionalPropertyMapper
@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class EndpointMetaModelMapper extends AdditionalPropertyMapper<EndpointMetaModelDto, EndpointMetaModelEntity, EndpointMetaModel> {

    @Autowired
    private EndpointResponseMetaModelMapper endpointResponseMetaModelMapper;

    @Autowired
    private DataStorageConnectorMetaModelMapper dataStorageConnectorMetaModelMapper;

    @Override
    @Mapping(target = "apiTag", ignore = true)
    @Mapping(target = "payloadMetamodel", ignore = true)
    @Mapping(target = "queryArguments", ignore = true)
    @Mapping(target = "serviceMetaModel", ignore = true)
    @Mapping(target = "responseMetaModel", ignore = true)
    public abstract EndpointMetaModel toMetaModel(EndpointMetaModelEntity endpointMetaModelEntity);

    public EndpointMetaModel toFullMetaModel(MetaModelContext metaModelContext, EndpointMetaModelEntity endpointMetaModelEntity) {
        EndpointMetaModel endpointMetaModel = toMetaModel(endpointMetaModelEntity);
        return endpointMetaModel.toBuilder()
            .apiTag(getFromContext(metaModelContext::getApiTags, () -> endpointMetaModelEntity.getApiTag().getId()))
            .urlMetamodel(BaseUrlModelResolver.resolveUrl(endpointMetaModelEntity.getBaseUrl()))
            .payloadMetamodel(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getPayloadMetamodel))
            .queryArguments(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getQueryArguments))
            .pathParams(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getPathParams))
            .serviceMetaModel(Optional.ofNullable(endpointMetaModelEntity.getServiceMetaModel())
                .map(serviceMetaModel -> getFromContext(metaModelContext::getServiceMetaModels, serviceMetaModel::getId))
                .orElse(metaModelContext.getDefaultServiceMetaModel()))
            .responseMetaModel(endpointResponseMetaModelMapper.toEndpointResponseMetaModel(metaModelContext, endpointMetaModelEntity.getResponseMetaModel()))
            .dataStorageConnectors(getStorageConnectors(metaModelContext, endpointMetaModelEntity))
            .build();
    }

    private List<DataStorageConnectorMetaModel> getStorageConnectors(MetaModelContext metaModelContext, EndpointMetaModelEntity endpointMetaModelEntity) {
        if (CollectionUtils.isNotEmpty(endpointMetaModelEntity.getDataStorageConnectors())) {
            return elements(endpointMetaModelEntity.getDataStorageConnectors())
                .map(dataStorageConnectorEntity ->
                    dataStorageConnectorMetaModelMapper.toFullMetaModel(metaModelContext, dataStorageConnectorEntity))
                .asList();
        } else {
            return metaModelContext.getDefaultDataStorageConnectorMetaModels();
        }
    }
}
