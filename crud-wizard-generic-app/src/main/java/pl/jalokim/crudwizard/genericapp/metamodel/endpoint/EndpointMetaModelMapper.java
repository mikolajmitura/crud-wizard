package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DataStorageResultsJoinerMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;
import pl.jalokim.crudwizard.genericapp.metamodel.url.UrlModelResolver;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.PropertyPathResolver;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.utils.collection.CollectionUtils;

// TODO try use uses to inject others mapper, now is problem with ambiguity from AdditionalPropertyMapper
@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class EndpointMetaModelMapper extends AdditionalPropertyMapper<EndpointMetaModelDto, EndpointMetaModelEntity, EndpointMetaModel> {

    @Autowired
    private EndpointResponseMetaModelMapper endpointResponseMetaModelMapper;

    @Autowired
    private DataStorageConnectorMetaModelMapper dataStorageConnectorMetaModelMapper;

    @Autowired
    private InstanceLoader instanceLoader;

    @Override
    @Mapping(target = "apiTag", ignore = true)
    @Mapping(target = "payloadMetamodel", ignore = true)
    @Mapping(target = "queryArguments", ignore = true)
    @Mapping(target = "pathParams", ignore = true)
    @Mapping(target = "serviceMetaModel", ignore = true)
    @Mapping(target = "responseMetaModel", ignore = true)
    @Mapping(target = "payloadMetamodelAdditionalValidators", ignore = true)
    @Mapping(target = "urlMetamodel", ignore = true)
    @Mapping(target = "dataStorageConnectors", ignore = true)
    public abstract EndpointMetaModel toMetaModel(EndpointMetaModelEntity endpointMetaModelEntity);

    // TODO #53 remove this after impl
    @Mapping(target = "mapperScript", ignore = true)
    @Mapping(target = "metamodelDtoType", ignore = true)
    public abstract MapperMetaModelDto toMapperMetaModelDto(MapperMetaModelEntity entity);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    public abstract ClassMetaModelDto classModelToDto(ClassMetaModelEntity classMetaModelEntity);

    public EndpointMetaModel toFullMetaModel(MetaModelContext metaModelContext, EndpointMetaModelEntity endpointMetaModelEntity) {
        EndpointMetaModel endpointMetaModel = toMetaModel(endpointMetaModelEntity);
        return endpointMetaModel.toBuilder()
            .apiTag(getFromContext(metaModelContext::getApiTags, () -> endpointMetaModelEntity.getApiTag().getId()))
            .urlMetamodel(UrlModelResolver.resolveUrl(endpointMetaModelEntity.getBaseUrl()))
            .payloadMetamodel(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getPayloadMetamodel))
            .queryArguments(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getQueryArguments))
            .pathParams(getFromContextByEntity(metaModelContext::getClassMetaModels, endpointMetaModelEntity::getPathParams))
            .serviceMetaModel(Optional.ofNullable(endpointMetaModelEntity.getServiceMetaModel())
                .map(serviceMetaModel -> getFromContext(metaModelContext::getServiceMetaModels, serviceMetaModel::getId))
                .orElse(metaModelContext.getDefaultServiceMetaModel()))
            .responseMetaModel(endpointResponseMetaModelMapper.toEndpointResponseMetaModel(metaModelContext, endpointMetaModelEntity))
            .dataStorageConnectors(getStorageConnectors(metaModelContext, endpointMetaModelEntity))
            .payloadMetamodelAdditionalValidators(createAdditionalValidatorsMetaModel(metaModelContext,
                endpointMetaModelEntity.getPayloadMetamodelAdditionalValidators()))
            .build();
    }

    @Mapping(target = "joinerVerifierInstance", source = "joinerVerifierClassName", qualifiedByName = "mapJoinerVerifierInstance")
    abstract DataStorageResultsJoinerMetaModel mapJoinEntry(DataStorageResultsJoinerEntity dataStorageResultsJoinerEntity);

    @Named("mapJoinerVerifierInstance")
    @SuppressWarnings("unchecked")
    protected ObjectsJoinerVerifier<Object, Object> mapJoinerVerifierInstance(String className) {
        return instanceLoader.createInstanceOrGetBean(className);
    }

    private List<DataStorageConnectorMetaModel> getStorageConnectors(MetaModelContext metaModelContext, EndpointMetaModelEntity endpointMetaModelEntity) {
        if (CollectionUtils.isNotEmpty(endpointMetaModelEntity.getDataStorageConnectors())) {
            return elements(endpointMetaModelEntity.getDataStorageConnectors())
                .map(dataStorageConnectorEntity -> dataStorageConnectorMetaModelMapper.toFullMetaModel(metaModelContext, dataStorageConnectorEntity))
                .asList();
        } else {
            return metaModelContext.getDefaultDataStorageConnectorMetaModels();
        }
    }

    static AdditionalValidatorsMetaModel createAdditionalValidatorsMetaModel(MetaModelContext metaModelContext,
        List<AdditionalValidatorsEntity> payloadAdditionalValidators) {
        AdditionalValidatorsMetaModel rootAdditionalValidatorsMetaModel = AdditionalValidatorsMetaModel.empty();
        for (AdditionalValidatorsEntity additionalValidatorsByPath : elements(payloadAdditionalValidators).asList()) {
            String fullPropertyPath = additionalValidatorsByPath.getFullPropertyPath();
            PropertyPath propertyPath = PropertyPathResolver.resolvePath(fullPropertyPath);
            List<PropertyPath> propertyPathsChain = propertyPath.getReversedPropertyPathsParts();
            AdditionalValidatorsMetaModel currentAdditionalValidators = rootAdditionalValidatorsMetaModel;
            for (int i = 0; i < propertyPathsChain.size(); i++) {
                PropertyPath currentProperty = propertyPathsChain.get(i);
                currentAdditionalValidators = currentAdditionalValidators.getOrCreateNextNode(currentProperty);
                if (CollectionUtils.isLastIndex(propertyPathsChain, i)) {
                    List<ValidatorMetaModel> validatorsMetaModel = currentAdditionalValidators.getValidatorsMetaModel();
                    elements(additionalValidatorsByPath.getValidators())
                        .map(validatorModelEntity -> metaModelContext.getValidatorMetaModels().getById(validatorModelEntity.getId()))
                        .forEach(validatorsMetaModel::add);
                }
            }
        }
        return rootAdditionalValidatorsMetaModel;
    }
}
