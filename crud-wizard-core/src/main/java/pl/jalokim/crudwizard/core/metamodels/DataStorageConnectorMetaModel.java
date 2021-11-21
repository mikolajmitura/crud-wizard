package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageConnectorMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;

    // TODO validation when added new mapper metamodel
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

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();

    public String getDataStorageName() {
        return dataStorageMetaModel.getName();
    }

    public DataStorage getDataStorage() {
        return dataStorageMetaModel.getDataStorage();
    }
}
