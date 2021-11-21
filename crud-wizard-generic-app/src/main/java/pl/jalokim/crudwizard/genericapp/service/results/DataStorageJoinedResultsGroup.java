package pl.jalokim.crudwizard.genericapp.service.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public class DataStorageJoinedResultsGroup {

    private final Map<String, List<JoinedResultsRow>> joinedResultsByDsQueryName = new HashMap<>();
    @Getter
    private final List<JoinedResultsRow> finalResultRows = new ArrayList<>();

    public void addRowsForQueryName(String queryName, List<JoinedResultsRow> rows) {
        var joinedResultsRows = joinedResultsByDsQueryName.computeIfAbsent(queryName, (key) -> new ArrayList<>());
        joinedResultsRows.addAll(rows);
    }

    public void assignQueryNameToGroup(String queryName) {
        joinedResultsByDsQueryName.put(queryName, new ArrayList<>());
    }

    public void addRowForQueryName(String queryName, JoinedResultsRow row) {
        joinedResultsByDsQueryName.get(queryName)
            .add(row);
    }

    public void addNotMatchedResultsRow(List<JoinedResultsRow> rows, Collection<String> queryNames) {
        for (String queryName : queryNames) {
            addRowsForQueryName(queryName, rows);
        }
        finalResultRows.addAll(rows);
    }

    public List<JoinedResultsRow> getRowsByQueryName(String queryName) {
        return joinedResultsByDsQueryName.get(queryName);
    }

    public Collection<String> getAssignedQueryNames() {
        return joinedResultsByDsQueryName.keySet();
    }
}
