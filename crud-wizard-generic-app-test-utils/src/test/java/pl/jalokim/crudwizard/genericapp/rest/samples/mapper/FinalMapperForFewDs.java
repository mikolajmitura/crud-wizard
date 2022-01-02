package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;

import java.util.HashMap;
import java.util.Map;
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;
import pl.jalokim.crudwizard.genericapp.service.results.JoinedResultsRow;

public class FinalMapperForFewDs {

    Object mapResults(GenericMapperArgument genericMapperArgument) {

        JoinedResultsRow joinedResultsRow = (JoinedResultsRow) genericMapperArgument.getSourceObject();
        var firstDsResult = joinedResultsRow.get(InMemoryDataStorage.DEFAULT_DS_NAME);
        var secondDsResult = joinedResultsRow.get("second-query");
        var thirdDsResult = joinedResultsRow.get("third-db");

        Map<String, Object> result = new HashMap<>();
        result.put("uuid2", getValueFromPath(secondDsResult, "uuid"));
        result.put("uuid3", getValueFromPath(thirdDsResult, "uuid"));
        result.put("name", getValueFromPath(secondDsResult, "name"));
        result.put("lastname", getValueFromPath(secondDsResult, "surname"));
        result.put("documentValue", getValueFromPath(firstDsResult, "document.value"));
        result.put("documentType", getValueFromPath(firstDsResult, "document.type"));
        result.put("personId", getValueFromPath(firstDsResult, "id"));

        return result;
    }
}
