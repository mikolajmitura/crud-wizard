package pl.jalokim.crudwizard.genericapp.validation.validator;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator.newValidator;
import static pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator.formatPlaceholderFor;

import java.util.List;
import java.util.Map;
import pl.jalokim.crudwizard.core.metamodels.PropertyPath;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;
import pl.jalokim.crudwizard.core.validation.javax.FieldMetadataExtractor;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class FieldShouldWhenOtherValidator extends BaseDataValidator<Map<String, Object>> {

    @Override
    protected boolean hasValidValue(Map<String, Object> value, ValidationSessionContext validationContext) {
        FieldMetadataExtractor fieldMetadataExtractor = new FieldMetadataExtractor() {
            @Override
            public String validatedStructureType() {
                return "metamodel";
            }

            @Override
            public String extractOwnerTypeName(Object targetObject, String fieldName) {
                return validationContext.getCurrentValidatorContext().getValidatedClassMetaModel().getName();
            }

            @Override
            @SuppressWarnings("unchecked")
            public Object extractValueOfField(Object targetObject, String fieldName) {
                return ((Map<String, Object>) targetObject).get(fieldName);
            }
        };

        String field = validationContext.getValidatorArgument("field");
        ExpectedFieldState should = validationContext.getValidatorArgument("should");
        List<String> fieldValues = validationContext.getValidatorArgumentOrDefault("fieldValues", List.of());
        String whenField = validationContext.getValidatorArgument("whenField");
        ExpectedFieldState is = validationContext.getValidatorArgument("is");
        List<String> otherFieldValues = validationContext.getValidatorArgumentOrDefault("otherFieldValues", List.of());

        FieldShouldWhenOtherCoreValidator fieldShouldWhenOtherValidator = newValidator(fieldMetadataExtractor, field, should, fieldValues,
            whenField, is, otherFieldValues);

        validationContext.addNextMessage(PropertyPath.createRoot().nextWithName(field), createMessagePlaceholder(messagePlaceholder(),
            fieldShouldWhenOtherValidator.getMessagePlaceholderArgs()));

        return fieldShouldWhenOtherValidator.isValidValue(value);
    }

    @Override
    public String messagePlaceholder() {
        return formatPlaceholderFor(FieldShouldWhenOther.class);
    }

    @Override
    public String validatorName() {
        return "FIELD_SHOULD_WHEN_OTHER";
    }
}
