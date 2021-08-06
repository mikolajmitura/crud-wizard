package pl.jalokim.crudwizard.genericapp.validation.generic;

import java.util.List;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;

@Component
public class GenericValidator {

    // TODO #03 invoke validation on fields and objects translated object
    public void validate(Object translatedObject, ClassMetaModel classMetaModel) {

    }

    // TODO #04 should validate for example payload with additionalValidators for example for not null
    public void validate(Object translatedObject, ClassMetaModel classMetaModel, List<ValidatorMetaModel> additionalValidators) {

    }
}
