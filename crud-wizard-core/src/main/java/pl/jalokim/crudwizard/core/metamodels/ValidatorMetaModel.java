package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ValidatorMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String realClassName;
}
