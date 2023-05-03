package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.BeforeClassValidationUpdater.attachFieldTranslationsWhenNotExist;
import static pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType.GENERATED;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.BeforeClassValidationUpdater;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto;
import pl.jalokim.utils.collection.CollectionUtils;

@Component
@RequiredArgsConstructor
public class BeforeEndpointValidatorUpdater {

    public void beforeValidation(EndpointMetaModelDto createEndpointMetaModelDto) {
        createDefaultClassMetaModelNames(createEndpointMetaModelDto);
        addTranslationsToClassMetaModels(createEndpointMetaModelDto);
        addQueryAndPathParamsClassMetaModelsToMappers(createEndpointMetaModelDto);
    }

    private void addTranslationsToClassMetaModels(EndpointMetaModelDto createEndpointMetaModelDto) {
        attachFieldTranslationsWhenNotExist(createEndpointMetaModelDto.getPayloadMetamodel());
        attachFieldTranslationsWhenNotExist(createEndpointMetaModelDto.getPathParams());
        attachFieldTranslationsWhenNotExist(createEndpointMetaModelDto.getQueryArguments());
        Optional.ofNullable(createEndpointMetaModelDto.getResponseMetaModel())
            .map(EndpointResponseMetaModelDto::getClassMetaModel)
            .ifPresent(BeforeClassValidationUpdater::attachFieldTranslationsWhenNotExist);
        elements(createEndpointMetaModelDto.getDataStorageConnectors())
            .forEach(dsConnector -> attachFieldTranslationsWhenNotExist(dsConnector.getClassMetaModelInDataStorage()));
    }

    private void addQueryAndPathParamsClassMetaModelsToMappers(EndpointMetaModelDto createEndpointMetaModelDto) {
        Optional.ofNullable(createEndpointMetaModelDto.getResponseMetaModel())
            .map(EndpointResponseMetaModelDto::getMapperMetaModel)
            .ifPresent(mapperMetaModelDto -> updateMapperMetaModelWithQueryAndPathParams(mapperMetaModelDto,
                createEndpointMetaModelDto.getPathParams(), createEndpointMetaModelDto.getQueryArguments()));

        if (CollectionUtils.isNotEmpty(createEndpointMetaModelDto.getDataStorageConnectors())) {
            for (var dataStorageConnector : createEndpointMetaModelDto.getDataStorageConnectors()) {

                Optional.ofNullable(dataStorageConnector.getMapperMetaModelForPersist())
                    .ifPresent(mapper -> updateMapperMetaModelWithQueryAndPathParams(mapper,
                        createEndpointMetaModelDto.getPathParams(), createEndpointMetaModelDto.getQueryArguments()));

                Optional.ofNullable(dataStorageConnector.getMapperMetaModelForQuery())
                    .ifPresent(mapper -> updateMapperMetaModelWithQueryAndPathParams(mapper,
                        createEndpointMetaModelDto.getPathParams(), createEndpointMetaModelDto.getQueryArguments()));
            }
        }
    }

    private void createDefaultClassMetaModelNames(EndpointMetaModelDto createEndpointMetaModelDto) {
        Optional.ofNullable(createEndpointMetaModelDto.getQueryArguments())
            .ifPresent(queryArguments -> queryArguments.setName(createClassModelName(createEndpointMetaModelDto, "QueryArguments")));

        Optional.ofNullable(createEndpointMetaModelDto.getPathParams())
            .ifPresent(pathParams -> pathParams.setName(createClassModelName(createEndpointMetaModelDto, "PathParams")));
    }

    private static String createClassModelName(EndpointMetaModelDto createEndpointMetaModelDto, String typeName) {
        return elements(createEndpointMetaModelDto.getBaseUrl(), typeName, createEndpointMetaModelDto.getOperationName())
            .asConcatText("_");
    }

    private void updateMapperMetaModelWithQueryAndPathParams(MapperMetaModelDto mapperMetaModelDto,
        ClassMetaModelDto pathParams, ClassMetaModelDto queryArguments) {

        if (GENERATED.equals(mapperMetaModelDto.getMapperType())) {
            Optional.ofNullable(mapperMetaModelDto.getMapperGenerateConfiguration())
                .ifPresent(mapperGenerateConfiguration -> {
                    mapperGenerateConfiguration.setPathVariablesClassModel(pathParams);
                    mapperGenerateConfiguration.setRequestParamsClassModel(queryArguments);
                });
        }
    }
}
