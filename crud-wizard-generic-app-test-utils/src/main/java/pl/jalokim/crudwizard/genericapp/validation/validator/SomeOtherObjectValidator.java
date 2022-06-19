package pl.jalokim.crudwizard.genericapp.validation.validator;

import static java.util.Optional.ofNullable;

import java.util.Map;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class SomeOtherObjectValidator extends BaseDataValidator<Map<String, Object>> {

    @Override
    protected boolean hasValidValue(Map<String, Object> value, ValidationSessionContext validationContext) {
        boolean isValid = true;
        String someField1Value = (String) ofNullable(value.get("someField1"))
            .orElse("");

        String someField2Value = (String) ofNullable(value.get("someField2"))
            .orElse("");

        if (someField1Value.length() < 3 && someField2Value.length() < 3) {
            validationContext.addNextMessage(PropertyPath.createRoot(), "SomeOtherObjectValidator.m1");
            isValid = false;
        }

        if (someField1Value.length() < 2) {
            validationContext.addNextMessage(PropertyPath.createRoot().nextWithName("someField1"),
                "SomeOtherObjectValidator.m2");
            isValid = false;
        }

        if (someField2Value.length() < 1) {
            validationContext.addNextMessage(PropertyPath.createRoot().nextWithName("someField2"),
                "SomeOtherObjectValidator.m3");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public String validatorName() {
        return "SOME-OTHER-OBJECT-TEST-VALIDATOR";
    }
}
