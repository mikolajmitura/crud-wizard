package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class FieldMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String fieldName;

    ClassMetaModel fieldType;

    List<ValidatorMetaModel> validators;
}
