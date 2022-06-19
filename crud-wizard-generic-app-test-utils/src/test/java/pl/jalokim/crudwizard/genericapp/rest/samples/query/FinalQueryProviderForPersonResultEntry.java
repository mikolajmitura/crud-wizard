package pl.jalokim.crudwizard.genericapp.rest.samples.query;

import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression;

public class FinalQueryProviderForPersonResultEntry implements DataStorageQueryProvider {

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {
        return DataStorageQuery.builder()
            .where(RealExpression.isNotNull("personId"))
            .build();
    }
}
