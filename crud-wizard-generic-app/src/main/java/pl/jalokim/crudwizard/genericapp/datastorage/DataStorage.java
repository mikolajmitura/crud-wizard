package pl.jalokim.crudwizard.genericapp.datastorage;

public interface DataStorage {

    default DataStorageTransactionProvider getTransactionProvider() {
        return null;
    }

}
