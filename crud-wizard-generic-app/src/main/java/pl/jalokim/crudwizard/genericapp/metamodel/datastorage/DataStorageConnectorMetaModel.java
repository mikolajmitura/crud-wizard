package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class DataStorageConnectorMetaModel extends AdditionalPropertyMetaModel {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;
    MapperMetaModel mapperMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
