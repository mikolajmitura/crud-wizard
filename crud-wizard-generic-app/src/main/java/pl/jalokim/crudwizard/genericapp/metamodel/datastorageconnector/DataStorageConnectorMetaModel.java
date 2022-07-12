package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import pl.jalokim.crudwizard.genericapp.datastorage.DataStorage;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class DataStorageConnectorMetaModel extends WithAdditionalPropertiesMetaModel {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;

    /**
     * Used during save to db or update for PUT and POST
     */
    MapperMetaModel mapperMetaModelForPersist;

    /**
     * Used only for get object by id. This is for mapping id value for valid with classMetaModelInDataStorage
     * This will be used when queryProvider is null
     */
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
