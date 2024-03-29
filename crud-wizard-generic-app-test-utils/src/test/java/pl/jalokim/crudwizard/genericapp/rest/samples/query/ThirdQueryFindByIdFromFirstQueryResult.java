package pl.jalokim.crudwizard.genericapp.rest.samples.query;


import static pl.jalokim.crudwizard.core.utils.ValueExtractorFromPath.getValueFromPath;

import java.util.List;
import java.util.stream.Collectors;
import pl.jalokim.crudwizard.datastorage.inmemory.InMemoryDataStorage;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression;

public class ThirdQueryFindByIdFromFirstQueryResult implements DataStorageQueryProvider {

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {

        List<Object> personIds = dataStorageQueryArguments.getPreviousQueryResultsContext()
            .get(InMemoryDataStorage.DEFAULT_DS_NAME).stream()
            .map(object -> getValueFromPath(object, "id"))
            .collect(Collectors.toList());

        return DataStorageQuery.builder()
            .selectFrom(dataStorageQueryArguments.getQueriedClassMetaModels().get(0))
            .where(RealExpression.in("firstDbId", personIds))
            .build();
    }
}
