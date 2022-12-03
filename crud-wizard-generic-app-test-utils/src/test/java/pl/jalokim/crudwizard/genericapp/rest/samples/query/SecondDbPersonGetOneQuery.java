package pl.jalokim.crudwizard.genericapp.rest.samples.query;

import static pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression.isEqualsTo;

import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;

public class SecondDbPersonGetOneQuery implements DataStorageQueryProvider {

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {
        return DataStorageQuery.builder()
            .selectFrom(dataStorageQueryArguments.getQueriedClassMetaModels().get(0))
            .where(isEqualsTo("firstDbId", dataStorageQueryArguments.getPathVariables().get("userId")))
            .build();
    }
}
