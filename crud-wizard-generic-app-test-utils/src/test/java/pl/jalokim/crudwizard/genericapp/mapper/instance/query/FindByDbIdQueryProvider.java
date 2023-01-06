package pl.jalokim.crudwizard.genericapp.mapper.instance.query;

import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQuery;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryArguments;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.datastorage.query.RealExpression;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.PersonCreateEvent;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory;

public class FindByDbIdQueryProvider implements DataStorageQueryProvider {

    @Override
    public DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments) {

        return DataStorageQuery.builder()
            .selectFrom(ClassMetaModelFactory.fromRawClass(PersonCreateEvent.class))
            .where(RealExpression.isEqualsTo("dbId", dataStorageQueryArguments.getPathVariables().get("userId")))
            .build();
    }
}
