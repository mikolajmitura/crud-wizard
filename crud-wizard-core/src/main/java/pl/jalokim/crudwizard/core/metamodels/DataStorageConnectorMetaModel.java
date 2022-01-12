package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class DataStorageConnectorMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;

    // TODO #1 validation when added new mapper metamodel
    // verify that this bean, class, method exists
    // verify that can method arguments will be resolved correctly
    // verify that newly added mapperMetaModel does not exists already, then use existing id
    MapperMetaModel mapperMetaModelForReturn;
    MapperMetaModel mapperMetaModelForQuery;
    ClassMetaModel classMetaModelInDataStorage;
    /**
     * when null then should be used name of data storage.
     */
    String nameOfQuery;
    DataStorageQueryProvider queryProvider;

    public String getDataStorageName() {
        return dataStorageMetaModel.getName();
    }

    public DataStorage getDataStorage() {
        return dataStorageMetaModel.getDataStorage();
    }
}
