package pl.jalokim.crudwizard.genericapp.datastorage.query;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Value
@Builder(toBuilder = true)
public class DataStorageQueryArguments {

    Map<String, String> headers;
    Map<String, Object> pathVariables;
    Map<String, Object> requestParams;

    Pageable pageable;
    Sort sortBy;

    // TODO #25 should be select from and should be relation to join other classMetaModel
    //  or join to DataStorageQuery
    List<ClassMetaModel> queriedClassMetaModels;
    ClassMetaModel requestParamsClassMetaModel;
    /**
     *  results of previous Data storages results.
     */
    Map<String, List<Object>> previousQueryResultsContext;
}
