package pl.jalokim.crudwizard.core.validation.javax.inner;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING_AND_COLLECTION_AND_NUMBER;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITHOUT_OTHER_FIELD_VALUES;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITH_OTHER_FIELD_VALUES;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.string.StringUtils.concat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;
import pl.jalokim.crudwizard.core.validation.javax.FieldMetadataExtractor;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.constants.Constants;

@RequiredArgsConstructor
@Getter
@SuppressWarnings({"PMD.GodClass", "PMD.NPathComplexity"})
public class FieldShouldWhenOtherCoreValidator {

    private final ExpectedFieldStatePredicates expectedFieldStatePredicates;
    private final String field;
    private final ExpectedFieldState should;
    private final List<String> fieldValues;
    private final String whenField;
    private final ExpectedFieldState is;
    private final List<String> otherFieldValues;

    public static FieldShouldWhenOtherCoreValidator newValidator(FieldMetadataExtractor fieldMetadataExtractor, String field,
        ExpectedFieldState should, List<String> fieldValues, String whenField, ExpectedFieldState is, List<String> otherFieldValues) {
        validateFieldConfiguration(ValidationFieldConfiguration.builder()
            .fieldByPositionName("field")
            .fieldByPositionValue(field)
            .expectedFieldStateFieldName("should")
            .expectedFieldState(should)
            .otherFieldValueName("fieldValues")
            .otherFieldValue(fieldValues)
            .annotationType(FieldShouldWhenOther.class)
            .build());

        validateFieldConfiguration(ValidationFieldConfiguration.builder()
            .fieldByPositionName("whenField")
            .fieldByPositionValue(whenField)
            .expectedFieldStateFieldName("is")
            .expectedFieldState(is)
            .otherFieldValueName("otherFieldValues")
            .otherFieldValue(otherFieldValues)
            .annotationType(FieldShouldWhenOther.class).build());

        var expectedFieldStatePredicates = new ExpectedFieldStatePredicates(fieldMetadataExtractor);

        return new FieldShouldWhenOtherCoreValidator(expectedFieldStatePredicates, field, should, fieldValues, whenField, is, otherFieldValues);
    }

    public boolean isValidValue(Object value) {
        boolean mainFieldResult = expectedFieldStatePredicates.testState(
            "field", field, should, "fieldValues", fieldValues, value);

        boolean otherFieldResult = expectedFieldStatePredicates.testState(
            "whenField", whenField, is, "otherFieldValues", otherFieldValues, value);

        if (otherFieldResult) {
            return mainFieldResult;
        }
        return true;
    }

    public Map<String, Object> getMessagePlaceholderArgs() {
        return Map.of(
            "should", getAppMessageSource().getMessageByEnumWithPrefix("shouldBe", should),
            "fieldValues", getValuesWhenCan(should, fieldValues),
            "whenField", wrapAsExternalPlaceholder(whenField),
            "is", getAppMessageSource().getMessageByEnumWithPrefix("whenIs", is),
            "otherFieldValues", getValuesWhenCan(is, otherFieldValues)
        );
    }

    public static void validateFieldConfiguration(ValidationFieldConfiguration validationFieldConfiguration) {
        if (WITHOUT_OTHER_FIELD_VALUES.contains(validationFieldConfiguration.getExpectedFieldState()) && CollectionUtils.isNotEmpty(
            validationFieldConfiguration.getOtherFieldValue())) {
            throw new IllegalArgumentException(String.format(
                "invalid @" + validationFieldConfiguration.getAnnotationType().getSimpleName() + " for %s=%s for: %s=%s, field: %s should be empty",
                validationFieldConfiguration.getFieldByPositionName(), validationFieldConfiguration.getFieldByPositionValue(),
                validationFieldConfiguration.getExpectedFieldStateFieldName(), validationFieldConfiguration.getExpectedFieldState(),
                validationFieldConfiguration.getOtherFieldValueName()));
        }

        if (WITH_OTHER_FIELD_VALUES.contains(validationFieldConfiguration.getExpectedFieldState()) && CollectionUtils.isEmpty(
            validationFieldConfiguration.getOtherFieldValue())) {
            throw new IllegalArgumentException(String.format(
                "invalid @" + validationFieldConfiguration.getAnnotationType().getSimpleName() + " for %s=%s for: %s=%s, field: %s should not be empty",
                validationFieldConfiguration.getFieldByPositionName(), validationFieldConfiguration.getFieldByPositionValue(),
                validationFieldConfiguration.getExpectedFieldStateFieldName(), validationFieldConfiguration.getExpectedFieldState(),
                validationFieldConfiguration.getOtherFieldValueName()));
        }

        if (FOR_STRING_AND_COLLECTION_AND_NUMBER.contains(validationFieldConfiguration.getExpectedFieldState()) && !canParseToNumber(
            validationFieldConfiguration.getOtherFieldValue())) {
            throw new IllegalArgumentException(String.format(
                "invalid @FieldShouldWhenOther for %s=%s for: %s=%s, field: %s should have only one element with number value",
                validationFieldConfiguration.getFieldByPositionName(), validationFieldConfiguration.getFieldByPositionValue(),
                validationFieldConfiguration.getExpectedFieldStateFieldName(), validationFieldConfiguration.getExpectedFieldState(),
                validationFieldConfiguration.getOtherFieldValueName()));
        }
    }

    public static String getValuesWhenCan(ExpectedFieldState otherFieldMatch, List<String> values) {
        if (List.of(NULL, NOT_NULL, EMPTY, EMPTY_OR_NULL, NOT_EMPTY, NOT_BLANK).contains(otherFieldMatch)) {
            return Constants.EMPTY;
        }
        return concat(SPACE, elements(values).asConcatText(", "));
    }

    static BigDecimal parseToNumber(List<String> numberValue) {
        if (numberValue == null || numberValue.size() != 1) {
            throw new NumberFormatException();
        }
        return new BigDecimal(numberValue.get(0));
    }

    private static boolean canParseToNumber(List<String> numberValue) {
        try {
            parseToNumber(numberValue);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
