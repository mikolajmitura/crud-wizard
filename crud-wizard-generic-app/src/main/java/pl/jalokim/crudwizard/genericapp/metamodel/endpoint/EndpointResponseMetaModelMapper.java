package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContext;
import static pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext.getFromContextByEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfigurationMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;

@Mapper(config = MapperAsSpringBeanConfig.class,
    uses = {
        AdditionalPropertyMapper.class,
        MapperGenerateConfigurationMapper.class,
        TranslationMapper.class,
        ClassMetaModelMapper.class
    })
public abstract class EndpointResponseMetaModelMapper
    implements BaseMapper<EndpointResponseMetaModelDto, EndpointResponseMetaModelEntity, EndpointResponseMetaModel> {

    // TODO #mappers make all fields in mapper as constructor arguments
    @Autowired
    private QueryProviderMapper queryProviderMapper;

    public EndpointResponseMetaModel toEndpointResponseMetaModel(MetaModelContext metaModelContext,
        EndpointMetaModelEntity endpointMetaModelEntity) {
        EndpointResponseMetaModelEntity endpointResponseMetaModelEntity = endpointMetaModelEntity.getResponseMetaModel();
        if (endpointResponseMetaModelEntity == null) {
            return null;
        }

        return toMetaModel(endpointResponseMetaModelEntity).toBuilder()
            .classMetaModel(getFromContext(metaModelContext::getClassMetaModels, () -> endpointResponseMetaModelEntity.getClassMetaModel().getId()))
            .mapperMetaModel(ofNullable(getFromContextByEntity(
                metaModelContext::getMapperMetaModels,
                endpointResponseMetaModelEntity::getMapperMetaModel))
                .orElseGet(() -> resolveDefaultFinalMapper(endpointMetaModelEntity, metaModelContext)))
            .queryProvider(queryProviderMapper.mapInstance(endpointResponseMetaModelEntity.getQueryProvider()))
            .build();
    }

    @Override
    @Mapping(target = "classMetaModel", ignore = true)
    @Mapping(target = "mapperMetaModel", ignore = true)
    @Mapping(target = "queryProvider", ignore = true)
    public abstract EndpointResponseMetaModel toMetaModel(EndpointResponseMetaModelEntity endpointResponseMetaModelEntity);

    // TODO #53 remove this after impl
    @Mapping(target = "mapperScript", ignore = true)
    @Mapping(target = "metamodelDtoType", ignore = true)
    public abstract MapperMetaModelDto toMapperMetaModelDto(MapperMetaModelEntity entity);

    MapperMetaModel resolveDefaultFinalMapper(EndpointMetaModelEntity endpointMetaModelEntity, MetaModelContext metaModelContext) {

        var responseClassModel = ofNullable(endpointMetaModelEntity.getResponseMetaModel())
            .map(EndpointResponseMetaModelEntity::getClassMetaModel)
            .orElse(null);
        if (responseClassModel == null) {
            return null;
        }
        if (endpointMetaModelEntity.getHttpMethod().equals(HttpMethod.POST)) {
            return metaModelContext.getDefaultExtractIdMapperMetaModel();
        }
        return metaModelContext.getDefaultFinalMapperMetaModel();
    }
}
