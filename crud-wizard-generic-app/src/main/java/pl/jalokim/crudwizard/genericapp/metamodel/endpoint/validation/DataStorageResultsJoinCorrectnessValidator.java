package pl.jalokim.crudwizard.genericapp.metamodel.endpoint.validation;

import static java.util.Optional.ofNullable;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsPlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.joinWithComma;
import static pl.jalokim.utils.collection.CollectionUtils.addWhenNotExist;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.DataStorageMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelTypeExtractor;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointResponseMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerDto;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.TypeMetadata;

@Component
@RequiredArgsConstructor
public class DataStorageResultsJoinCorrectnessValidator
    implements BaseConstraintValidator<DataStorageResultsJoinCorrectness, EndpointMetaModelDto> {

    public static final String DATA_STORAGE_RESULTS_JOINERS = "dataStorageResultsJoiners";
    private final DataStorageConnectorMetaModelRepository dataStorageConnectorMetaModelRepository;
    private final MetaModelContextService metaModelContextService;
    private final ClassMetaModelTypeExtractor classMetaModelTypeExtractor;

    @Override
    public boolean isValidValue(EndpointMetaModelDto endpointMetaModelDto, ConstraintValidatorContext context) {
        var httpMethod = endpointMetaModelDto.getHttpMethod();
        Class<?> endpointResponseClass = ofNullable(endpointMetaModelDto.getResponseMetaModel())
            .map(EndpointResponseMetaModelDto::getClassMetaModel)
            .map(ClassMetaModelDto::getClassName)
            .map(ClassUtils::loadRealClass)
            .orElse(null);

        if (HttpMethod.GET.equals(httpMethod) && endpointResponseClass != null
            && (isCollectionType(endpointResponseClass) || MetadataReflectionUtils.isTypeOf(endpointResponseClass, Page.class))) {
            List<DataStorageConnectorMetaModelDto> dataStorageConnectors = elements(endpointMetaModelDto.getDataStorageConnectors()).asList();
            List<DataStorageResultsJoinerDto> dataStorageResultsJoiners = elements(endpointMetaModelDto.getDataStorageResultsJoiners()).asList();

            int numberOfConnectors = dataStorageConnectors.size();
            int numberOfJoiners = dataStorageResultsJoiners.size();
            if (numberOfConnectors <= 1 && numberOfJoiners == 0) {
                return true;
            }

            Map<String, ClassMetaModelDto> classMetaModelDtoByDsQueryName = new HashMap<>();

            return expectedNumberOfJoinersDueToDsConnectors(context, numberOfConnectors, numberOfJoiners)
                && namesAreCorrect(dataStorageConnectors, dataStorageResultsJoiners, context, classMetaModelDtoByDsQueryName, endpointMetaModelDto)
                && allJoinerCorrectlyConnected(dataStorageResultsJoiners, context)
                && allPathsCorrectAndTypeMatched(dataStorageResultsJoiners, context, classMetaModelDtoByDsQueryName);
        }
        return true;
    }

    private boolean expectedNumberOfJoinersDueToDsConnectors(ConstraintValidatorContext context, int numberOfConnectors, int numberOfJoiners) {
        var isValid = numberOfConnectors - 1 == numberOfJoiners;
        if (!isValid) {
            customMessage(context,
                wrapAsPlaceholder(DataStorageResultsJoinCorrectness.class, "invalidJoinersNumber"),
                PropertyPath.builder()
                    .addNextProperty(DATA_STORAGE_RESULTS_JOINERS)
                    .build());
        }
        return isValid;
    }

    private boolean namesAreCorrect(List<DataStorageConnectorMetaModelDto> dataStorageConnectors,
        List<DataStorageResultsJoinerDto> dataStorageResultsJoiners, ConstraintValidatorContext context,
        Map<String, ClassMetaModelDto> classMetaModelDtoByDsQueryName,
        EndpointMetaModelDto endpointMetaModelDto) {

        AtomicBoolean namesAreCorrect = new AtomicBoolean(true);
        elements(dataStorageConnectors).forEachWithIndex((index, dsConnector) -> {

            Pair<String, ClassMetaModelDto> queryOrDsNameWithReturnType = findQueryOrDsName(dsConnector, endpointMetaModelDto);

            if (classMetaModelDtoByDsQueryName.containsKey(queryOrDsNameWithReturnType.getKey())) {
                customMessage(context,
                    wrapAsPlaceholder(DataStorageResultsJoinCorrectness.class, "nonUniqueDsOrQueryName"),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex("dataStorageConnectors", index)
                        .build());
                namesAreCorrect.set(false);
            } else {
                classMetaModelDtoByDsQueryName.put(queryOrDsNameWithReturnType.getKey(),
                    queryOrDsNameWithReturnType.getValue());
            }
        });

        elements(dataStorageResultsJoiners).forEachWithIndex((index, joinerDto) -> {

            if (!classMetaModelDtoByDsQueryName.containsKey(joinerDto.getLeftNameOfQueryResult())) {
                customMessage(context,
                    wrapAsPlaceholder(DataStorageResultsJoinCorrectness.class, "notFound"),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex(DATA_STORAGE_RESULTS_JOINERS, index)
                        .addNextProperty("leftNameOfQueryResult")
                        .build());
                namesAreCorrect.set(false);
            }

            if (!classMetaModelDtoByDsQueryName.containsKey(joinerDto.getRightNameOfQueryResult())) {
                customMessage(context,
                    wrapAsPlaceholder(DataStorageResultsJoinCorrectness.class, "notFound"),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex(DATA_STORAGE_RESULTS_JOINERS, index)
                        .addNextProperty("rightNameOfQueryResult")
                        .build());
                namesAreCorrect.set(false);
            }
        });

        return namesAreCorrect.get();
    }

    private Pair<String, ClassMetaModelDto> findQueryOrDsName(DataStorageConnectorMetaModelDto dsConnector,
        EndpointMetaModelDto endpointMetaModelDto) {
        ClassMetaModelDto responseClassMetaModel = endpointMetaModelDto.getResponseMetaModel().getClassMetaModel()
            .getGenericTypes().get(0);

        return ofNullable(dsConnector.getId())
            .map(dataStorageConnectorMetaModelRepository::findExactlyOneById)
            .flatMap(connectorEntity ->
                ofNullable(connectorEntity.getNameOfQuery())
                    .or(() -> ofNullable(connectorEntity.getDataStorageMetaModel())
                        .map(DataStorageMetaModelEntity::getName))
                    .map(dsOrQueryName -> Pair.of(dsOrQueryName, ofNullable(connectorEntity.getClassMetaModelInDataStorage())
                        .map(classMetaModelEntity -> ClassMetaModelDto.builder()
                            .id(classMetaModelEntity.getId())
                            .build())
                        .orElse(responseClassMetaModel))))
            .or(() -> ofNullable(dsConnector.getNameOfQuery())
                .or(() -> ofNullable(dsConnector.getDataStorageMetaModel())
                    .map(DataStorageMetaModelDto::getName))
                .or(() -> ofNullable(dsConnector.getDataStorageMetaModel())
                    .map(DataStorageMetaModelDto::getId)
                    .map(id -> metaModelContextService.getMetaModelContext().getDataStorages().getById(id))
                    .map(DataStorageMetaModel::getName))
                .map(dsOrQueryName -> Pair.of(dsOrQueryName, ofNullable(dsConnector.getClassMetaModelInDataStorage())
                    .orElse(responseClassMetaModel))))
            .orElseGet(() -> Pair.of(getNameOfDefaultDataStorage(), responseClassMetaModel));
    }

    @SuppressWarnings("PMD.ConfusingTernary")
    private boolean allJoinerCorrectlyConnected(List<DataStorageResultsJoinerDto> dataStorageResultsJoiners, ConstraintValidatorContext context) {
        List<List<String>> groupsOfDsResults = new ArrayList<>();
        AtomicBoolean correctlyConnected = new AtomicBoolean(true);

        elements(dataStorageResultsJoiners).forEachWithIndex((index, dataStorageResultsJoiner) -> {
            String leftNameOfQueryResult = dataStorageResultsJoiner.getLeftNameOfQueryResult();
            List<String> leftGroup = findGroupByDsResultName(groupsOfDsResults, leftNameOfQueryResult);

            String rightNameOfQueryResult = dataStorageResultsJoiner.getRightNameOfQueryResult();
            List<String> rightGroup = findGroupByDsResultName(groupsOfDsResults, rightNameOfQueryResult);

            List<String> groupForAdd;
            if (leftGroup == null && rightGroup == null) {
                groupForAdd = new ArrayList<>();
                groupsOfDsResults.add(groupForAdd);
            } else if (Objects.equals(rightGroup, leftGroup)) {
                groupForAdd = leftGroup;
                correctlyConnected.set(false);
                customMessage(context,
                    createMessagePlaceholder(DataStorageResultsJoinCorrectness.class, "existsInTheSameGroupAlready", joinWithComma(groupForAdd)),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex(DATA_STORAGE_RESULTS_JOINERS, index)
                        .build());
            } else {
                if (leftGroup != null && rightGroup != null) {
                    groupForAdd = leftGroup;
                    leftGroup.addAll(rightGroup);
                    groupsOfDsResults.remove(rightGroup);
                } else {
                    groupForAdd = Elements.of(leftGroup, rightGroup)
                        .filter(Objects::nonNull)
                        .getFirst();
                }
            }
            addWhenNotExist(groupForAdd, leftNameOfQueryResult);
            addWhenNotExist(groupForAdd, rightNameOfQueryResult);
        });

        if (groupsOfDsResults.size() != 1) {
            customMessage(context,
                createMessagePlaceholder(DataStorageResultsJoinCorrectness.class, "notOneGroupResults",
                    elements(groupsOfDsResults).asConcatText(", ")),
                PropertyPath.builder()
                    .addNextProperty(DATA_STORAGE_RESULTS_JOINERS)
                    .build());
        }

        return correctlyConnected.get();
    }

    private boolean allPathsCorrectAndTypeMatched(List<DataStorageResultsJoinerDto> dataStorageResultsJoiners,
        ConstraintValidatorContext context, Map<String, ClassMetaModelDto> classMetaModelDtoByDsQueryName) {
        AtomicBoolean allPathsCorrect = new AtomicBoolean(true);
        elements(dataStorageResultsJoiners).forEachWithIndex((index, joinEntry) -> {

            Optional<TypeMetadata> leftJoinType = getTypeByPath(context, classMetaModelDtoByDsQueryName, allPathsCorrect,
                joinEntry.getLeftNameOfQueryResult(), joinEntry.getLeftPath(), index, "leftPath");

            Optional<TypeMetadata> rightJoinType = getTypeByPath(context, classMetaModelDtoByDsQueryName, allPathsCorrect,
                joinEntry.getRightNameOfQueryResult(), joinEntry.getRightPath(), index, "rightPath");

            if (leftJoinType.isPresent() && rightJoinType.isPresent() && !leftJoinType.get().equals(rightJoinType.get())) {
                customMessage(context, createMessagePlaceholder(DataStorageResultsJoinCorrectness.class, "notTheSameTypesForJoin",
                    Map.of("leftType", leftJoinType.get().getCanonicalName(),
                        "rightType", rightJoinType.get().getCanonicalName())),
                    PropertyPath.builder()
                        .addNextPropertyAndIndex(DATA_STORAGE_RESULTS_JOINERS, index)
                        .build());
                allPathsCorrect.set(false);
            }
        });
        return allPathsCorrect.get();
    }

    private Optional<TypeMetadata> getTypeByPath(ConstraintValidatorContext context, Map<String, ClassMetaModelDto> classMetaModelDtoByDsQueryName,
        AtomicBoolean allPathsCorrect, String nameOfQueryResult, String joinPath, Integer joinerIndex, String pathFieldName) {

        ClassMetaModelDto classMetaModelDto = classMetaModelDtoByDsQueryName.get(nameOfQueryResult);
        try {
            return classMetaModelTypeExtractor.getTypeByPath(classMetaModelDto, joinPath);
        } catch (TechnicalException ex) {
            customMessage(context, ex.getMessage(),
                PropertyPath.builder()
                    .addNextPropertyAndIndex(DATA_STORAGE_RESULTS_JOINERS, joinerIndex)
                    .addNextProperty(pathFieldName)
                    .build());
            allPathsCorrect.set(false);
            return Optional.empty();
        }
    }

    private List<String> findGroupByDsResultName(List<List<String>> groupsOfDsResults, String dsResultName) {
        return elements(groupsOfDsResults)
            .filter(group -> group.contains(dsResultName))
            .getFirstOrNull();
    }

    private String getNameOfDefaultDataStorage() {
        return metaModelContextService.getMetaModelContext().getDefaultDataStorageMetaModel().getName();
    }

}
