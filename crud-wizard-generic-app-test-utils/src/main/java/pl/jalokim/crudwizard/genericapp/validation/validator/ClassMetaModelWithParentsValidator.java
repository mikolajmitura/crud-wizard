package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.test.utils.translations.ValidationMessageConstants.NOT_BLANK_MESSAGE_PROPERTY;

import java.util.Map;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;
import pl.jalokim.utils.string.StringUtils;

public class ClassMetaModelWithParentsValidator extends BaseDataValidator<Map<String, Object>> {

    @Override
    protected boolean hasValidValue(Map<String, Object> value, ValidationSessionContext validationContext) {
        validationContext.addNextMessage(PropertyPath.createRoot(), "ClassMetaModelWithParentsValidator.m1");

        String someUniqueValue = (String) value.get("someUnique");
        if (StringUtils.isBlank(someUniqueValue)) {
            validationContext.addNextMessage(PropertyPath.createRoot().nextWithName("someUnique"), NOT_BLANK_MESSAGE_PROPERTY);
            return false;
        }

        return true;
    }

    @Override
    public String validatorName() {
        return "PARENT-METAMODEL-TEST-VALIDATOR";
    }
}
