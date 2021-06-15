package pl.jalokim.crudwizard.core.datastorage;

public interface DataStorageTransactionProvider {

    void startTransaction();

    void commitTransaction();

    void rollbackTransaction(Exception cause);
}
