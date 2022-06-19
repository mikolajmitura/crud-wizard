package pl.jalokim.crudwizard.genericapp.datastorage.query;

public interface DataStorageQueryProvider {

    DataStorageQuery createQuery(DataStorageQueryArguments dataStorageQueryArguments);
}
