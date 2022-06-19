package pl.jalokim.crudwizard.genericapp.datastorage;

public interface DataStorageTransactionProvider {

    void startTransaction();

    void commitTransaction();

    void rollbackTransaction(Throwable cause);
}
