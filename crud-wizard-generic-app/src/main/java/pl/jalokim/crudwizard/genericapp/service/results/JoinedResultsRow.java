package pl.jalokim.crudwizard.genericapp.service.results;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

public class JoinedResultsRow {

    @Getter
    private final Map<String, Object> joinedResultsByDsQueryName = new LinkedHashMap<>();

    public void addJoinedObject(String dsQueryName, Object resultEntry) {
        joinedResultsByDsQueryName.put(dsQueryName, resultEntry);
    }

    public static JoinedResultsRow newJoinedResultsRow(String dsQueryName, Object value) {
        JoinedResultsRow joinedResultsRow = new JoinedResultsRow();
        joinedResultsRow.addJoinedObject(dsQueryName, value);
        return joinedResultsRow;
    }

    Object getValueByQueryName(String dsQueryName) {
        return joinedResultsByDsQueryName.get(dsQueryName);
    }

    boolean containsValueByQueryName(String queryName) {
        return joinedResultsByDsQueryName.containsKey(queryName);
    }

    public Object get(String queryResultName) {
        return joinedResultsByDsQueryName.get(queryResultName);
    }

    public boolean containsQueryResultsByName(String queryResultName) {
        return joinedResultsByDsQueryName.containsKey(queryResultName);
    }
}
