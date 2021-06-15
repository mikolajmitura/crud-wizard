package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ValidatorMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String realClassName;
}
