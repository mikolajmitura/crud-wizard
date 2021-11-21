package pl.jalokim.crudwizard.core.datastorage.query;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public interface DataStorageQueryProvider {

    DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments, ClassMetaModel queriedClassMetaModel);
}
