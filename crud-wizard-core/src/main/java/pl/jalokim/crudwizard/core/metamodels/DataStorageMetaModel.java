package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DataStorageMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    DataStorage dataStorage;
}
