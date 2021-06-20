package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DataStorageMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;

}
