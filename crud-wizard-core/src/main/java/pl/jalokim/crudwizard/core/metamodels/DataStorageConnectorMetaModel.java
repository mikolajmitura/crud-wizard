package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class DataStorageConnectorMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;
    DataStorageMetaModel dataStorageMetaModel;

    // TODO validation when added new mapper metamodel
    // verify that this bean, class, method exists
    // verify that newly added mapperMetaModel does not exists already, then use existing id
    MapperMetaModel mapperMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
