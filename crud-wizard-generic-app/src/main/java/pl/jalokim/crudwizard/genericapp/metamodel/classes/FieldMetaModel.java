package pl.jalokim.crudwizard.genericapp.metamodel.classes;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class FieldMetaModel extends ParentMetaModel {

    Long id;

    String fieldName;

    ClassMetaModel fieldType;

    List<ValidatorMetaModel> validators;
}
