package pl.jalokim.crudwizard.genericapp.metamodel.datastorage.query;

import java.util.Map;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
public class DefaultDataStorageQueryProvider implements DataStorageQueryProvider {

    Map<String, Object> queryArguments;
    ClassMetaModel queryClassMetaModel;

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments, ClassMetaModel classMetaModelFromDataStore) {
        // TODO #37 fetch all not null values from requestParams make as likeignorecase
        //  or fetch compare type from additional properties via requestParamsClassMetaModel for concrete field.
        //  and between every value use 'and' predicate upon meta model of type classMetaModelFromDataStore
        Map<String, Object> requestParams = dataStorageQueryArguments.getRequestParams();
        ClassMetaModel requestParamsClassMetaModel = dataStorageQueryArguments.getRequestParamsClassMetaModel();

        return null;
    }
}
