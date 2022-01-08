package pl.jalokim.crudwizard.core.datastorage.query;

public interface DataStorageQueryProvider {

    DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments);
}
