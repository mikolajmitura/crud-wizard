package pl.jalokim.crudwizard.core.datastorage.query;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
@Builder(toBuilder = true)
public class DataStorageQueryArguments {

    Map<String, String> headers;
    Map<String, Object> pathVariables;
    Map<String, Object> requestParams;
    List<ClassMetaModel> queriedClassMetaModels;
    ClassMetaModel requestParamsClassMetaModel;
    /**
     *  results of previous Data storages results.
     */
    Map<String, List<Object>> previousQueryResultsContext;
}
