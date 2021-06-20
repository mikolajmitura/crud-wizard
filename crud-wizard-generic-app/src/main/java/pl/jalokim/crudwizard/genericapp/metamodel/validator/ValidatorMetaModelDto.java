package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ValidatorMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    String className;
}
