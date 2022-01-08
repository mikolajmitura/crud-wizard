package pl.jalokim.crudwizard.genericapp.rest.samples.query;

import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.datastorage.query.RealExpression;

public class FinalQueryProvider1 implements DataStorageQueryProvider {

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {
        return DataStorageQuery.builder()
            .where(RealExpression.likeIgnoreCase("name", "John"))
            .build();
    }
}
