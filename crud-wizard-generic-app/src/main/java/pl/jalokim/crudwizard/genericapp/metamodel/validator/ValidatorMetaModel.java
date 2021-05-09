package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ParentMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
public class ValidatorMetaModel extends ParentMetaModel {

    Long id;

    String realClassName;
}
