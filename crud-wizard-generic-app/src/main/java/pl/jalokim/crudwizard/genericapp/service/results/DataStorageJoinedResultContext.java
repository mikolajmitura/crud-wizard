package pl.jalokim.crudwizard.genericapp.service.results;

import static pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow.newJoinedResultsRow;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import pl.jalokim.utils.collection.CollectionUtils;

public class DataStorageJoinedResultContext {

    private final List<DataStorageJoinedResultsGroup> joinedResultsGroups = new ArrayList<>();

    public void createNewGroup(String nameOfQueryResult, List<Object> queryResults) {
        DataStorageJoinedResultsGroup dataStorageJoinedResultsGroup = new DataStorageJoinedResultsGroup();
        List<JoinedResultsRow> newRows = elements(queryResults)
            .map(dsResultRow -> newJoinedResultsRow(nameOfQueryResult, dsResultRow))
            .asList();
        dataStorageJoinedResultsGroup.addNotMatchedResultsRow(newRows, List.of(nameOfQueryResult));

        joinedResultsGroups.add(dataStorageJoinedResultsGroup);
    }

    public boolean alreadyInContext(String dsQueryNameResult) {
        return getJoinGroupByQueryName(dsQueryNameResult) != null;
    }

    public DataStorageJoinedResultsGroup getJoinGroupByQueryName(String queryName) {
        return elements(joinedResultsGroups)
            .filter(group -> group.getAssignedQueryNames().contains(queryName))
            .getFirstOrNull();
    }

    public void assignQueryNamesFromGroup2ToGroup1(DataStorageJoinedResultsGroup group1, DataStorageJoinedResultsGroup group2) {
        var queryNamesForUpdate = group2.getAssignedQueryNames();
        for (String queryName : queryNamesForUpdate) {
            group1.addRowsForQueryName(queryName, group2.getRowsByQueryName(queryName));
        }
        joinedResultsGroups.remove(group2);
    }

    public List<JoinedResultsRow> getAllRows() {
        if (joinedResultsGroups.size() != 1) {
            throw new IllegalStateException("should exists only one group");
        }
        return CollectionUtils.getFirst(joinedResultsGroups).getFinalResultRows();
    }
}
