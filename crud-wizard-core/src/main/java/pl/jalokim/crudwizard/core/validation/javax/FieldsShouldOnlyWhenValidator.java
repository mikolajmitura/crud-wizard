package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage.buildMessageForValidator;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldMetadataExtractorFromClass.FIELD_META_EXTRACTOR_FROM_CLASS;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator.getValuesWhenCan;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator.newValidator;
import static pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator.validateFieldConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidatorWithDynamicMessage;
import pl.jalokim.crudwizard.core.validation.javax.inner.ExpectedFieldStatePredicates;
import pl.jalokim.crudwizard.core.validation.javax.inner.FieldShouldWhenOtherCoreValidator;
import pl.jalokim.crudwizard.core.validation.javax.inner.ValidationFieldConfiguration;

public class FieldsShouldOnlyWhenValidator implements BaseConstraintValidatorWithDynamicMessage<WhenFieldIsInStateThenOthersShould, Object> {

    private final ExpectedFieldStatePredicates expectedFieldStatePredicates = new ExpectedFieldStatePredicates(FIELD_META_EXTRACTOR_FROM_CLASS);
    private String whenField;
    private ExpectedFieldState is;
    private List<String> fieldValues;
    private List<FieldShouldWhenOther> thenOthersShould;

    @Override
    public void initialize(WhenFieldIsInStateThenOthersShould fieldsShouldOnlyWhen) {
        whenField = fieldsShouldOnlyWhen.whenField();
        is = fieldsShouldOnlyWhen.is();
        fieldValues = Arrays.asList(fieldsShouldOnlyWhen.fieldValues());
        thenOthersShould = Arrays.asList(fieldsShouldOnlyWhen.thenOthersShould());

        validateFieldConfiguration(ValidationFieldConfiguration.builder()
            .fieldByPositionName("whenField")
            .fieldByPositionValue(whenField)
            .expectedFieldStateFieldName("is")
            .expectedFieldState(is)
            .otherFieldValueName("fieldValues")
            .otherFieldValue(fieldValues)
            .annotationType(WhenFieldIsInStateThenOthersShould.class)
            .build());
    }

    @Override
    public void setupCustomMessage(Object value, ConstraintValidatorContext context) {

    }

    @Override
    public boolean isValidValue(Object value, ConstraintValidatorContext context) {
        boolean whenFieldHasExpectedState = expectedFieldStatePredicates.testState(
            "whenField", whenField, is, "fieldValues", fieldValues, value);

        boolean isValid = true;
        if (whenFieldHasExpectedState) {
            for (FieldShouldWhenOther fieldShouldWhenOther : thenOthersShould) {
                FieldShouldWhenOtherCoreValidator fieldShouldWhenOtherValidator = newValidator(
                    FIELD_META_EXTRACTOR_FROM_CLASS,
                    fieldShouldWhenOther.field(),
                    fieldShouldWhenOther.should(),
                    Arrays.asList(fieldShouldWhenOther.fieldValues()),
                    fieldShouldWhenOther.whenField(),
                    fieldShouldWhenOther.is(),
                    Arrays.asList(fieldShouldWhenOther.otherFieldValues()));
                boolean nestedIsInExpectedState = fieldShouldWhenOtherValidator.isValidValue(value);
                if (!nestedIsInExpectedState) {
                    String nestedMessage = createMessagePlaceholder(buildMessageForValidator(FieldShouldWhenOther.class),
                        fieldShouldWhenOtherValidator.getMessagePlaceholderArgs()).translateMessage();

                    String rootMessage = createMessagePlaceholder(
                        messagePlaceholder(context), Map.of(
                            "nestedMessage", nestedMessage,
                            "whenField", wrapAsExternalPlaceholder(whenField),
                            "is", getAppMessageSource().getMessageByEnumWithPrefix("whenIs", is),
                            "fieldValues", getValuesWhenCan(is, fieldValues)
                        )
                    ).translateMessage();

                    customMessage(context, rootMessage, fieldShouldWhenOtherValidator.getField());
                }
                isValid = isValid && nestedIsInExpectedState;
            }
        }

        return isValid;
    }
}
