package pl.jalokim.crudwizard.genericapp.service;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isAbstractClassOrInterface;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTypeOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.config.jackson.SimplePageImpl;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments.DataStorageQueryArgumentsBuilder;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.datastorage.query.inmemory.InMemoryDsQueryRunner;
import pl.jalokim.crudwizard.core.exception.BusinessLogicException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.DataStorageConnectorMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EndpointResponseMetaModel;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument.GenericMapperArgumentBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.MapperDelegatorService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.service.results.DataStorageResultJoiner;
import pl.jalokim.crudwizard.genericapp.service.translator.DefaultClassesConfig;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;

@Service
@RequiredArgsConstructor
@GenericService
public class DefaultGenericService {

    private final MapperDelegatorService mapperDelegatorService;
    private final DataStorageResultJoiner dataStorageResultJoiner;
    private final DefaultClassesConfig defaultClassesConfig;
    private final InMemoryDsQueryRunner inMemoryDsQueryRunner;
    private final MetaModelContextService metaModelContextService;

    @GenericMethod
    public Object saveOrReadFromDataStorages(GenericServiceArgument genericServiceArgument) {
        EndpointMetaModel endpointMetaModel = genericServiceArgument.getEndpointMetaModel();
        Map<String, Object> resultsByDataStorageName = new HashMap<>();
        HttpMethod httpMethod = genericServiceArgument.getEndpointMetaModel().getHttpMethod();

        EndpointResponseMetaModel responseMetaModel = Optional.ofNullable(endpointMetaModel.getResponseMetaModel())
            .orElse(EndpointResponseMetaModel.EMPTY);

        ClassMetaModel responseClassMetaModel = responseMetaModel.getClassMetaModel();
        Object responseBody = null;

        GenericMapperArgumentFactory genericMapperArgumentFactory = new GenericMapperArgumentFactory(genericServiceArgument, resultsByDataStorageName);

        DataStorageQueryArgumentsFactory dataStorageQueryArgumentsFactory = new DataStorageQueryArgumentsFactory(genericServiceArgument);

        // TODO #38 invoke validation that path variable values are correct in whole url... search for 'TODO #38'

        if (httpMethod.equals(HttpMethod.POST) || httpMethod.equals(HttpMethod.PATCH) || httpMethod.equals(HttpMethod.PUT)) {
            createOrUpdate(genericServiceArgument, endpointMetaModel, resultsByDataStorageName, genericMapperArgumentFactory);
        } else if (httpMethod.equals(HttpMethod.DELETE)) {
            delete(genericServiceArgument, endpointMetaModel, resultsByDataStorageName,
                genericMapperArgumentFactory, dataStorageQueryArgumentsFactory);
        } else if (httpMethod.equals(HttpMethod.GET)) {
            Class<?> responseRealClass = responseClassMetaModel.getRealClass();
            if (responseRealClass != null) {
                if (isTypeOf(responseRealClass, Page.class)) {
                    return returnPage(genericServiceArgument, endpointMetaModel, responseClassMetaModel,
                        dataStorageQueryArgumentsFactory, genericMapperArgumentFactory, responseMetaModel);
                } else if (isTypeOf(responseRealClass, Collection.class)) {
                    return returnCollection(endpointMetaModel, responseMetaModel, responseClassMetaModel,
                        genericMapperArgumentFactory, responseRealClass, dataStorageQueryArgumentsFactory);
                }
            } else {
                getResultById(genericServiceArgument, endpointMetaModel, resultsByDataStorageName, responseClassMetaModel,
                    genericMapperArgumentFactory, dataStorageQueryArgumentsFactory);
            }
        }

        if (responseClassMetaModel != null) {
            GenericMapperArgument finalResultMapperArgument = genericMapperArgumentFactory.create()
                .sourceObject(resultsByDataStorageName)
                .targetMetaModel(responseClassMetaModel)
                .build();

            responseBody = mapperDelegatorService.mapToTarget(responseMetaModel.getMapperMetaModel(), finalResultMapperArgument);
        }

        return responseBody;
    }

    private Page<Object> returnPage(GenericServiceArgument genericServiceArgument, EndpointMetaModel endpointMetaModel,
        ClassMetaModel responseClassMetaModel, DataStorageQueryArgumentsFactory dataStorageQueryArgumentsFactory,
        GenericMapperArgumentFactory genericMapperArgumentFactory, EndpointResponseMetaModel responseMetaModel) {

        ClassMetaModel elementTypeInCollection = responseClassMetaModel.getGenericTypes().get(0);
        Map<String, List<Object>> queriesResults = new HashMap<>();

        var dataStorageConnectors = endpointMetaModel.getDataStorageConnectors();

        DataStorageConnectorMetaModel mainDataStorageConnectorModel = dataStorageConnectors.get(0);

        var query = extractDataStorageQuery(dataStorageQueryArgumentsFactory, elementTypeInCollection,
            queriesResults, mainDataStorageConnectorModel);

        Pageable pageable = Optional.ofNullable(genericServiceArgument.getPageable())
            .orElse(Pageable.unpaged());
        Page<Object> mainPageOfObjects = mainDataStorageConnectorModel.getDataStorage().findPageOfEntity(pageable, query);

        String nameOfMainPageQueryResult = Optional.ofNullable(mainDataStorageConnectorModel.getNameOfQuery())
            .orElseGet(mainDataStorageConnectorModel::getDataStorageName);
        List<Object> objects = queriesResults.computeIfAbsent(nameOfMainPageQueryResult, (k) -> new ArrayList<>());
        objects.addAll(mainPageOfObjects.getContent());

        for (int i = 1; i < dataStorageConnectors.size(); i++) {
            extractResultsFromDataStorage(dataStorageQueryArgumentsFactory, elementTypeInCollection,
                queriesResults, dataStorageConnectors.get(i));
        }

        Page<Object> pageResult;
        if (queriesResults.size() > 1) {
            List<Object> joinedRows = elements(dataStorageResultJoiner.getJoinedNodes(
                endpointMetaModel.getDataStorageResultsJoiners(), queriesResults))
                .filter(element -> element.containsQueryResultsByName(nameOfMainPageQueryResult))
                .map(element -> (Object) element)
                .asList();

            pageResult = new SimplePageImpl<>(joinedRows, mainPageOfObjects.getNumber(),
                mainPageOfObjects.getSize(), mainPageOfObjects.getTotalElements());
        } else {
            pageResult = mainPageOfObjects;
        }

        return pageResult
            .map(element -> {
                GenericMapperArgument finalResultMapperArgument = genericMapperArgumentFactory.create()
                    .mappingContext(new HashMap<>())
                    .sourceObject(element)
                    .targetMetaModel(elementTypeInCollection)
                    .build();
                return mapperDelegatorService.mapToTarget(responseMetaModel.getMapperMetaModel(), finalResultMapperArgument);
            });
    }

    private void getResultById(GenericServiceArgument genericServiceArgument, EndpointMetaModel endpointMetaModel, Map<String, Object> resultsByDataStorageName,
        ClassMetaModel responseClassMetaModel, GenericMapperArgumentFactory genericMapperArgumentFactory,
        DataStorageQueryArgumentsFactory dataStorageQueryArgumentsFactory) {

        String lastVariableNameInUrl = endpointMetaModel.getUrlMetamodel().getLastVariableNameInUrl();
        Object idOfObject = genericServiceArgument.getUrlPathParams().get(lastVariableNameInUrl);
        for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
            getResultFromDataStorageAndPutToContext(resultsByDataStorageName, genericMapperArgumentFactory,
                idOfObject, dataStorageConnector, responseClassMetaModel, dataStorageQueryArgumentsFactory);
        }
    }

    private Collection<Object> returnCollection(EndpointMetaModel endpointMetaModel, EndpointResponseMetaModel responseMetaModel,
        ClassMetaModel responseClassMetaModel, GenericMapperArgumentFactory genericMapperArgumentFactory,
        Class<?> responseRealClass, DataStorageQueryArgumentsFactory dataStorageQueryArguments) {

        ClassMetaModel elementTypeInCollection = responseClassMetaModel.getGenericTypes().get(0);
        Map<String, List<Object>> queriesResults = new HashMap<>();

        for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
            extractResultsFromDataStorage(dataStorageQueryArguments, elementTypeInCollection, queriesResults, dataStorageConnector);
        }

        Collection<Object> results;
        if (queriesResults.size() > 1) {
            results = new ArrayList<>(dataStorageResultJoiner.getJoinedNodes(
                endpointMetaModel.getDataStorageResultsJoiners(), queriesResults));
        } else {
            results = elements(queriesResults.values()).getFirst();
        }

        results = elements(results)
            .map(element -> {
                GenericMapperArgument finalResultMapperArgument = genericMapperArgumentFactory.create()
                    .mappingContext(new HashMap<>())
                    .sourceObject(element)
                    .targetMetaModel(elementTypeInCollection)
                    .build();
                return mapperDelegatorService.mapToTarget(responseMetaModel.getMapperMetaModel(), finalResultMapperArgument);
            }).asList();

        results = runFinalQueryWhenShould(dataStorageQueryArguments.create().build(),
            endpointMetaModel.getResponseMetaModel().getQueryProvider(), results);

        Class<?> nonAbstractCollectionClass = responseRealClass;
        if (isAbstractClassOrInterface(responseRealClass)) {
            nonAbstractCollectionClass = defaultClassesConfig.returnConfig().get(responseRealClass);
        }
        Collection<Object> finalCollection = (Collection<Object>) InvokableReflectionUtils.newInstance(nonAbstractCollectionClass);
        finalCollection.addAll(results);
        return finalCollection;
    }

    private void extractResultsFromDataStorage(DataStorageQueryArgumentsFactory dataStorageQueryArguments, ClassMetaModel elementTypeInCollection,
        Map<String, List<Object>> queriesResults, DataStorageConnectorMetaModel dataStorageConnector) {
        var query = extractDataStorageQuery(dataStorageQueryArguments, elementTypeInCollection,
            queriesResults, dataStorageConnector);

        List<Object> queryResults = dataStorageConnector.getDataStorage().findEntities(query);
        String nameOfQueryResult = Optional.ofNullable(dataStorageConnector.getNameOfQuery())
            .orElseGet(dataStorageConnector::getDataStorageName);
        List<Object> objects = queriesResults.computeIfAbsent(nameOfQueryResult, (k) -> new ArrayList<>());
        objects.addAll(queryResults);
    }

    private DataStorageQuery extractDataStorageQuery(DataStorageQueryArgumentsFactory dataStorageQueryArguments, ClassMetaModel elementTypeInCollection,
        Map<String, List<Object>> queriesResults, DataStorageConnectorMetaModel dataStorageConnector) {
        DataStorageQueryProvider queryProvider = Optional.ofNullable(dataStorageConnector.getQueryProvider())
            .orElseGet(() -> metaModelContextService.getMetaModelContext().getDefaultDataStorageQueryProvider());

        ClassMetaModel queriedClassMetaModel = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
            .orElse(elementTypeInCollection);

        return queryProvider.createQuery(dataStorageQueryArguments.create()
            .queriedClassMetaModels(List.of(queriedClassMetaModel))
            .previousQueryResultsContext(queriesResults)
            .build());
    }

    private void delete(GenericServiceArgument genericServiceArgument, EndpointMetaModel endpointMetaModel, Map<String, Object> resultsByDataStorageName,
        GenericMapperArgumentFactory genericMapperArgumentFactory, DataStorageQueryArgumentsFactory dataStorageQueryArgumentsFactory) {

        String lastVariableNameInUrl = endpointMetaModel.getUrlMetamodel().getLastVariableNameInUrl();
        Object idOfObject = genericServiceArgument.getUrlPathParams().get(lastVariableNameInUrl);

        for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {

            var mappedIdForDataStorage = getResultFromDataStorageAndPutToContext(resultsByDataStorageName,
                genericMapperArgumentFactory, idOfObject, dataStorageConnector, null, dataStorageQueryArgumentsFactory);

            if (mappedIdForDataStorage.getMappedId() != null) {
                mappedIdForDataStorage.getDataStorage().deleteEntity(dataStorageConnector.getClassMetaModelInDataStorage(),
                    mappedIdForDataStorage.getMappedId());
            } else {
                mappedIdForDataStorage.getDataStorage().delete(mappedIdForDataStorage.getQuery());
            }
        }
    }

    private void createOrUpdate(GenericServiceArgument genericServiceArgument, EndpointMetaModel endpointMetaModel,
        Map<String, Object> resultsByDataStorageName,
        GenericMapperArgumentFactory genericMapperArgumentFactory) {
        for (DataStorageConnectorMetaModel dataStorageConnector : endpointMetaModel.getDataStorageConnectors()) {
            String dataStorageName = dataStorageConnector.getDataStorageName();

            ClassMetaModel targetMetaModel = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
                .orElse(endpointMetaModel.getPayloadMetamodel());

            GenericMapperArgument mapperArgument = genericMapperArgumentFactory.create()
                .sourceObject(genericServiceArgument.getRequestBodyTranslated().getRealValue())
                .sourceMetaModel(endpointMetaModel.getPayloadMetamodel())
                .targetMetaModel(targetMetaModel)
                .build();

            Object mappedObjectForDs = mapperDelegatorService.mapToTarget(dataStorageConnector.getMapperMetaModelForReturn(), mapperArgument);
            Object currentOrNewId = dataStorageConnector.getDataStorage().saveOrUpdate(targetMetaModel, mappedObjectForDs);
            resultsByDataStorageName.put(dataStorageName, currentOrNewId);
        }
    }

    private Collection<Object> runFinalQueryWhenShould(DataStorageQueryArguments dataStorageQueryArguments,
        DataStorageQueryProvider queryProvider, Collection<Object> results) {
        if (queryProvider == null) {
            return results;
        }
        return inMemoryDsQueryRunner.runQuery(results.stream(), queryProvider.createQuery(dataStorageQueryArguments));
    }

    private ResultInDataStorageFoundBy getResultFromDataStorageAndPutToContext(Map<String, Object> resultsByDataStorageName,
        GenericMapperArgumentFactory genericMapperArgumentFactory, Object idOfObject,
        DataStorageConnectorMetaModel dataStorageConnector, ClassMetaModel otherReturnClassModel,
        DataStorageQueryArgumentsFactory dataStorageQueryArgumentsFactory) {

        GenericMapperArgument mapperArgument = genericMapperArgumentFactory.create()
            .sourceObject(idOfObject)
            .build();

        ClassMetaModel classMetaModelInDataStorage = Optional.ofNullable(dataStorageConnector.getClassMetaModelInDataStorage())
            .orElse(otherReturnClassModel);

        DataStorage dataStorage = dataStorageConnector.getDataStorageMetaModel().getDataStorage();

        DataStorageQueryProvider queryProvider = dataStorageConnector.getQueryProvider();

        if (queryProvider != null) {
            DataStorageQuery query = queryProvider.createQuery(dataStorageQueryArgumentsFactory.create()
                .queriedClassMetaModels(List.of(classMetaModelInDataStorage))
                .build());

            List<Object> found = dataStorage.findEntities(query);
            if (found.size() > 1) {
                throw new BusinessLogicException("{found.more.than.one.result}");
            }
            resultsByDataStorageName.put(dataStorage.getName(), found.get(0));
            return ResultInDataStorageFoundBy.of(dataStorage, null, query);
        }
        Object mappedId = mapperDelegatorService.mapToTarget(dataStorageConnector.getMapperMetaModelForQuery(), mapperArgument);

        resultsByDataStorageName.put(dataStorage.getName(), dataStorage.getEntityById(classMetaModelInDataStorage, mappedId));
        return ResultInDataStorageFoundBy.of(dataStorage, mappedId, null);

    }

    @Value(staticConstructor = "of")
    private static class ResultInDataStorageFoundBy {

        DataStorage dataStorage;
        Object mappedId;
        DataStorageQuery query;
    }

    @RequiredArgsConstructor
    private static class GenericMapperArgumentFactory {

        private final GenericServiceArgument genericServiceArgument;
        private final Map<String, Object> resultsByDataStorageName;

        public GenericMapperArgumentBuilder create() {
            return GenericMapperArgument.builder()
                .headers(genericServiceArgument.getHeaders())
                .pathVariables(genericServiceArgument.getUrlPathParams())
                .requestParams(genericServiceArgument.getHttpQueryTranslated())
                .mappingContext(resultsByDataStorageName);
        }
    }

    @RequiredArgsConstructor
    private static class DataStorageQueryArgumentsFactory {

        private final GenericServiceArgument genericServiceArgument;

        public DataStorageQueryArgumentsBuilder create() {
            return DataStorageQueryArguments.builder()
                .headers(genericServiceArgument.getHeaders())
                .pathVariables(genericServiceArgument.getUrlPathParams())
                .requestParams(genericServiceArgument.getHttpQueryTranslated())
                .requestParamsClassMetaModel(genericServiceArgument.getEndpointMetaModel().getQueryArguments())
                .pageable(genericServiceArgument.getPageable())
                .sortBy(genericServiceArgument.getSortBy());
        }
    }
}
