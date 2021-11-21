package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument.GenericMapperArgumentBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.MapperDelegatorService;
import pl.jalokim.crudwizard.genericapp.service.results.DataStorageResultJoiner;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRowMapper;
import pl.jalokim.crudwizard.genericapp.service.translator.DefaultClassesConfig;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Service
@RequiredArgsConstructor
@GenericService
public class DefaultGenericService {

    private final MapperDelegatorService mapperDelegatorService;
    private final DataStorageResultJoiner dataStorageResultJoiner;
    private final DefaultClassesConfig defaultClassesConfig;
    private final JoinedResultsRowMapper joinedResultsRowMapper;

    @GenericMethod
    public Object saveOrReadFromDataStorages(GenericServiceArgument genericServiceArgument) {
        // TODO save or read from data storages
        // when save to ds then get object map by mapper and save
        // when read then map by mapper and return
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        Map<String, Object> resultsByDataStorageName = new HashMap<>();
        HttpMethod httpMethod = genericServiceArgument.getEndpointMetaModel().getHttpMethod();

        EndpointResponseMetaModel responseMetaModel = Optional.ofNullable(endpointMetaModel.getResponseMetaModel())
            .orElse(EndpointResponseMetaModel.EMPTY);

        ClassMetaModel responseClassMetaModel = responseMetaModel.getClassMetaModel();
        Object responseBody = null;

        GenericMapperArgumentFactory genericMapperArgumentFactory = new GenericMapperArgumentFactory(genericServiceArgument, resultsByDataStorageName);

        // TODO invoke validation that path variable values are correct in whole url... search for 'TODO #38'

        if (httpMethod.equals(HttpMethod.POST) || httpMethod.equals(HttpMethod.PATCH) || httpMethod.equals(HttpMethod.PUT)) {
            for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
                String dataStorageName = dataStorageConnector.getDataStorageName();
                ClassMetaModel targetMetaModel = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
                    .orElse(endpointMetaModel.getPayloadMetamodel());
                GenericMapperArgument mapperArgument = genericMapperArgumentFactory.get()
                    .sourceObject(genericServiceArgument.getRequestBodyTranslated().getRealValue())
                    .sourceMetaModel(endpointMetaModel.getPayloadMetamodel())
                    .targetMetaModel(targetMetaModel)
                    .build();
                Object mappedObjectForDs = mapperDelegatorService.mapToTarget(dataStorageConnector.getMapperMetaModelForReturn(), mapperArgument);
                Object newId = dataStorageConnector.getDataStorage().saveEntity(targetMetaModel, mappedObjectForDs);
                resultsByDataStorageName.put(dataStorageName, newId);
            }
        } else if (httpMethod.equals(HttpMethod.DELETE)) {
            String lastVariableNameInUrl = endpointMetaModel.getUrlMetamodel().getLastVariableNameInUrl();
            Object idOfObject = genericServiceArgument.getUrlPathParams().get(lastVariableNameInUrl);

            for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {

                var mappedIdForDataStorage = getResultFromDataStorageAndPutToContext(resultsByDataStorageName,
                    genericMapperArgumentFactory, idOfObject, dataStorageConnector, null);
                mappedIdForDataStorage.getDataStorage().deleteEntity(dataStorageConnector.getClassMetaModelInDataStorage(),
                    mappedIdForDataStorage.getMappedId());
            }
        } else if (httpMethod.equals(HttpMethod.GET)) {
            Class<?> responseRealClass = responseClassMetaModel.getRealClass();
            ClassMetaModel elementTypeInCollection = responseClassMetaModel.getGenericTypes().get(0);
            if (responseRealClass != null) {
                if (isTypeOf(responseRealClass, Page.class)) {
                    // TODO #37 return collection when request params cannot be mapped to Pageable
                } else if (isTypeOf(responseRealClass, Collection.class)) {
                    // TODO #37 return Page or collection when request params can be mapped to Pageable
                    Map<String, List<Object>> queriesResults = new HashMap<>();
                    DataStorageQueryArguments dataStorageQueryArguments = DataStorageQueryArguments.builder()
                        .headers(genericServiceArgument.getHeaders())
                        .pathVariables(genericServiceArgument.getUrlPathParams())
                        .requestParams(genericServiceArgument.getHttpQueryParams())
                        .requestParamsClassMetaModel(genericServiceArgument.getEndpointMetaModel().getQueryArguments())
                        .previousQueryResultsContext(queriesResults)
                        .build();

                    for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
                        DataStorageQueryProvider queryProvider = dataStorageConnector.getQueryProvider();
                        ClassMetaModel queriedClassMetaModel = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
                            .orElse(elementTypeInCollection);

                        DataStorageQuery query = queryProvider.createQuery(dataStorageQueryArguments, queriedClassMetaModel);
                        List<Object> queryResults = dataStorageConnector.getDataStorage().findEntities(queriedClassMetaModel, query);
                        List<Object> objects = queriesResults.computeIfAbsent(Optional.ofNullable(dataStorageConnector.getNameOfQuery())
                            .orElse(dataStorageConnector.getDataStorageName()), (k) -> new ArrayList<>());
                        objects.addAll(queryResults);
                    }

                    List<Object> resultsForFinalMapping;
                    if (queriesResults.size() > 1) {
                        List<JoinedResultsRow> joinedNodes = dataStorageResultJoiner.getJoinedNodes(
                            endpointMetaModel.getDataStorageResultsJoiners(), queriesResults);
                        resultsForFinalMapping = new ArrayList<>();
                        for (JoinedResultsRow joinedNode : joinedNodes) {
                            resultsForFinalMapping.add(joinedResultsRowMapper.mapToObject(joinedNode));
                        }
                    } else {
                        resultsForFinalMapping = elements(queriesResults.values()).getFirst();
                    }

                    Collection<Object> results = new ArrayList<>();
                    for (Object entry : resultsForFinalMapping) {
                        GenericMapperArgument finalResultMapperArgument = genericMapperArgumentFactory.get()
                            .mappingContext(new HashMap<>())
                            .sourceObject(entry)
                            .targetMetaModel(elementTypeInCollection)
                            .build();

                        results.add(mapperDelegatorService.mapToTarget(responseMetaModel.getMapperMetaModel(), finalResultMapperArgument));
                    }

                    results = runFinalQueryWhenShould(endpointMetaModel.getResponseMetaModel().getQueryProvider(), results);

                    Class<?> nonAbstractCollectionClass = responseRealClass;
                    if (MetadataReflectionUtils.isConcreteClass(responseRealClass)) {
                        nonAbstractCollectionClass = defaultClassesConfig.returnConfig().get(responseRealClass);
                    }
                    Collection<Object> finalCollection = (Collection<Object>) InvokableReflectionUtils.newInstance(nonAbstractCollectionClass);
                    finalCollection.addAll(results);
                    return finalCollection;
                }
            } else {
                String lastVariableNameInUrl = endpointMetaModel.getUrlMetamodel().getLastVariableNameInUrl();
                Object idOfObject = genericServiceArgument.getUrlPathParams().get(lastVariableNameInUrl);
                for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
                    getResultFromDataStorageAndPutToContext(resultsByDataStorageName, genericMapperArgumentFactory,
                        idOfObject, dataStorageConnector, responseClassMetaModel);
                }
            }
        }

        if (responseClassMetaModel != null) {
            GenericMapperArgument finalResultMapperArgument = genericMapperArgumentFactory.get()
                .sourceObject(resultsByDataStorageName)
                .targetMetaModel(responseClassMetaModel)
                .build();

            responseBody = mapperDelegatorService.mapToTarget(responseMetaModel.getMapperMetaModel(), finalResultMapperArgument);
        }

        return responseBody;
    }

    private Collection<Object> runFinalQueryWhenShould(DataStorageQueryProvider queryProvider, Collection<Object> results) {
        if (queryProvider == null) {
            return results;
        }
        // TODO use final query, some order or filter
        throw new UnsupportedOperationException("not supported final query");
    }

    private MappedIdForDataStorage getResultFromDataStorageAndPutToContext(Map<String, Object> resultsByDataStorageName,
        GenericMapperArgumentFactory genericMapperArgumentFactory, Object idOfObject,
        DataStorageConnectorMetaModel dataStorageConnector, ClassMetaModel otherReturnClassModel) {

        GenericMapperArgument mapperArgument = genericMapperArgumentFactory.get()
            .sourceObject(idOfObject)
            .build();
        Object mappedId = mapperDelegatorService.mapToTarget(dataStorageConnector.getMapperMetaModelForQuery(), mapperArgument);
        DataStorage dataStorage = dataStorageConnector.getDataStorageMetaModel().getDataStorage();
        ClassMetaModel classMetaModelInDataStorage = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
            .orElse(otherReturnClassModel);
        resultsByDataStorageName.put(dataStorage.getName(), dataStorage.getEntityById(classMetaModelInDataStorage, mappedId));
        return MappedIdForDataStorage.of(dataStorage, mappedId);
    }

    @Value(staticConstructor = "of")
    private static class MappedIdForDataStorage {

        DataStorage dataStorage;
        Object mappedId;
    }

    @RequiredArgsConstructor
    private static class GenericMapperArgumentFactory implements Supplier<GenericMapperArgument.GenericMapperArgumentBuilder> {

        private final GenericServiceArgument genericServiceArgument;
        private final Map<String, Object> resultsByDataStorageName;

        @Override
        public GenericMapperArgumentBuilder get() {
            return GenericMapperArgument.builder()
                .headers(genericServiceArgument.getHeaders())
                .pathVariables(genericServiceArgument.getUrlPathParams())
                .requestParams(genericServiceArgument.getHttpQueryTranslated())
                .mappingContext(resultsByDataStorageName);
        }
    }
}
