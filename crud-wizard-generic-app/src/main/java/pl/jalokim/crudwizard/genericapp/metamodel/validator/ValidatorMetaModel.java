package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ValidatorMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String realClassName;
}
