package pl.jalokim.crudwizard.genericapp.service.results;

import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;
import static pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow.newJoinedResultsRow;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.datastorage.query.ObjectsJoinerVerifier;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query.DataStorageResultsJoinerMetaModel;

@Component
@SuppressWarnings({
    "PMD.ConfusingTernary",
    "PMD.CollapsibleIfStatements",
    "PMD.CognitiveComplexity"
})
public class DataStorageResultJoiner {

    public List<JoinedResultsRow> getJoinedNodes(List<DataStorageResultsJoinerMetaModel> dataStorageResultsJoiners, Map<String, List<Object>> queriesResults) {
        DataStorageJoinedResultContext joinedResultContext = new DataStorageJoinedResultContext();

        for (DataStorageResultsJoinerMetaModel dsResultsJoiner : dataStorageResultsJoiners) {
            String leftQueryName = dsResultsJoiner.getLeftNameOfQueryResult();
            String rightQueryName = dsResultsJoiner.getRightNameOfQueryResult();

            if (!joinedResultContext.alreadyInContext(leftQueryName)
                && !joinedResultContext.alreadyInContext(rightQueryName)) {

                List<Object> leftDataStorageResults = queriesResults.get(leftQueryName);
                joinedResultContext.createNewGroup(leftQueryName, leftDataStorageResults);

                joinResultsFromGroupWithDsQueryResults(joinedResultContext,
                    QueryNameWithPath.of(dsResultsJoiner.getLeftPath(), leftQueryName),
                    QueryNameWithPath.of(dsResultsJoiner.getRightPath(), rightQueryName),
                    dsResultsJoiner.getJoinerVerifierInstance(), queriesResults);

            } else if (joinedResultContext.alreadyInContext(leftQueryName)
                && joinedResultContext.alreadyInContext(rightQueryName)) {
                joinResultsWhenBothSidesExistsInContext(joinedResultContext, dsResultsJoiner);

            } else if (joinedResultContext.alreadyInContext(leftQueryName)
                && !joinedResultContext.alreadyInContext(rightQueryName)) {
                joinResultsFromGroupWithDsQueryResults(joinedResultContext,
                    QueryNameWithPath.of(dsResultsJoiner.getLeftPath(), leftQueryName),
                    QueryNameWithPath.of(dsResultsJoiner.getRightPath(), rightQueryName),
                    dsResultsJoiner.getJoinerVerifierInstance(), queriesResults);

            } else {
                joinResultsFromGroupWithDsQueryResults(joinedResultContext,
                    QueryNameWithPath.of(dsResultsJoiner.getRightPath(), rightQueryName),
                    QueryNameWithPath.of(dsResultsJoiner.getLeftPath(), leftQueryName),
                    dsResultsJoiner.getJoinerVerifierInstance(), queriesResults);
            }
        }
        return joinedResultContext.getAllRows();
    }

    private void joinResultsWhenBothSidesExistsInContext(DataStorageJoinedResultContext joinedResultContext,
        DataStorageResultsJoinerMetaModel dsResultsJoiner) {

        String leftQueryName = dsResultsJoiner.getLeftNameOfQueryResult();
        String rightQueryName = dsResultsJoiner.getRightNameOfQueryResult();

        var leftGroup = joinedResultContext.getJoinGroupByQueryName(leftQueryName);
        var rightGroup = joinedResultContext.getJoinGroupByQueryName(rightQueryName);
        rightGroup.getAssignedQueryNames().forEach(leftGroup::assignQueryNameToGroup);

        List<JoinedResultsRow> notMatchedRows = new ArrayList<>();


        for (JoinedResultsRow rightResultRow : rightGroup.getFinalResultRows()) {
            boolean matchedRow = false;
            if (rightResultRow.containsValueByQueryName(rightQueryName)) {
                Object fieldValueFromRight = getValueFromPath(
                    rightResultRow.getValueByQueryName(rightQueryName),
                    dsResultsJoiner.getRightPath());

                for (JoinedResultsRow leftResultRow : leftGroup.getRowsByQueryName(leftQueryName)) {

                    Object fieldValueFromLeft = getValueFromPath(
                        leftResultRow.getValueByQueryName(leftQueryName),
                        dsResultsJoiner.getLeftPath());

                    if (fieldValueFromLeft != null) {
                        if (fieldValueFromRight != null) {
                            var joinerVerifierInstance = dsResultsJoiner.getJoinerVerifierInstance();
                            if (joinerVerifierInstance.areJoined(fieldValueFromLeft, fieldValueFromRight)) {
                                matchedRow = true;
                                for (var entry : rightResultRow.getJoinedResultsByDsQueryName().entrySet()) {
                                    leftResultRow.addJoinedObject(entry.getKey(), entry.getValue());
                                    leftGroup.addRowForQueryName(entry.getKey(), leftResultRow);
                                }
                            }
                        }
                    }
                }
            }
            if (!matchedRow) {
                notMatchedRows.add(rightResultRow);
            }
        }

        leftGroup.addNotMatchedResultsRow(notMatchedRows, rightGroup.getAssignedQueryNames());
        joinedResultContext.assignQueryNamesFromGroup2ToGroup1(leftGroup, rightGroup);
    }

    private void joinResultsFromGroupWithDsQueryResults(DataStorageJoinedResultContext joinedResultContext,
        QueryNameWithPath queryNameWithPathInContext, QueryNameWithPath queryNameWithPathNotInContext,
        ObjectsJoinerVerifier<Object, Object> joinerVerifierInstance, Map<String, List<Object>> queriesResults) {

        String queryNameInContext = queryNameWithPathInContext.getQueryName();
        String pathInContext = queryNameWithPathInContext.getFieldFullPath();
        String queryNameNotInContext = queryNameWithPathNotInContext.getQueryName();
        String pathNotInContext = queryNameWithPathNotInContext.getFieldFullPath();

        var joinGroup = joinedResultContext.getJoinGroupByQueryName(queryNameInContext);
        joinGroup.assignQueryNameToGroup(queryNameNotInContext);
        List<Object> objectNotJoined = new ArrayList<>();

        for (Object objectFromRight : queriesResults.get(queryNameNotInContext)) {
            boolean matchedRow = false;
            Object fieldValueFromRight = getValueFromPath(objectFromRight, pathNotInContext);
            if (fieldValueFromRight != null) {
                for (var joinedResultsRow : joinGroup.getRowsByQueryName(queryNameInContext)) {
                    Object fieldValueFromLeft = getValueFromPath(joinedResultsRow.getValueByQueryName(queryNameInContext), pathInContext);
                    if (fieldValueFromLeft != null) {
                        if (joinerVerifierInstance.areJoined(fieldValueFromLeft, fieldValueFromRight)) {
                            matchedRow = true;
                            joinGroup.addRowForQueryName(queryNameNotInContext, joinedResultsRow);
                            joinedResultsRow.addJoinedObject(queryNameNotInContext, objectFromRight);
                        }
                    }
                }
            }
            if (!matchedRow) {
                objectNotJoined.add(objectFromRight);
            }
        }

        List<JoinedResultsRow> newRows = elements(objectNotJoined)
            .map(dsResultRow -> newJoinedResultsRow(queryNameNotInContext, dsResultRow))
            .asList();
        joinGroup.addNotMatchedResultsRow(newRows, List.of(queryNameNotInContext));
    }

    @Value(staticConstructor = "of")
    private static class QueryNameWithPath {

        String fieldFullPath;
        String queryName;
    }
}
