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
    MapperMetaModel mapperMetaModel;
    ClassMetaModel classMetaModelInDataStorage;
}
