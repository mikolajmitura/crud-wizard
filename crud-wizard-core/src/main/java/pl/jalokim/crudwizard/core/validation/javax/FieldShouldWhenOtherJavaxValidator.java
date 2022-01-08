package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldMetadataExtractorFromClass.FIELD_META_EXTRACTOR_FROM_CLASS;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator.newValidator;

import java.util.Arrays;
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator;

public class FieldShouldWhenOtherJavaxValidator implements BaseConstraintValidatorWithDynamicMessage<FieldShouldWhenOther, Object> {

    private FieldShouldWhenOtherCoreValidator fieldShouldWhenOtherValidator;

    @Override
    public void initialize(FieldShouldWhenOther fieldShouldWhenOther) {

        fieldShouldWhenOtherValidator = newValidator(
            FIELD_META_EXTRACTOR_FROM_CLASS,
            fieldShouldWhenOther.field(),
            fieldShouldWhenOther.should(),
            Arrays.asList(fieldShouldWhenOther.fieldValues()),
            fieldShouldWhenOther.whenField(),
            fieldShouldWhenOther.is(),
            Arrays.asList(fieldShouldWhenOther.otherFieldValues())
        );
    }

    @Override
    public void setupCustomMessage(Object value, ConstraintValidatorContext context) {
        customMessage(context, createMessagePlaceholder(
            messagePlaceholder(context), messagePlaceholderArgs(value, context)
        ).translateMessage(), fieldShouldWhenOtherValidator.getField());
    }

    @Override
    public boolean isValidValue(Object value, ConstraintValidatorContext context) {
        return fieldShouldWhenOtherValidator.isValidValue(value);
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(Object value, ConstraintValidatorContext context) {
        return fieldShouldWhenOtherValidator.getMessagePlaceholderArgs();
    }
}
