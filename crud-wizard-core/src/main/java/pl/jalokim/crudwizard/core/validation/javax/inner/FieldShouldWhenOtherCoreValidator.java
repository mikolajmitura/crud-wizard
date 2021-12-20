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

import java.lang.annotation.Annotation;
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
        validateFieldConfiguration("field", field,
            "should", should,
            "fieldValues", fieldValues, FieldShouldWhenOther.class);

        validateFieldConfiguration("whenField", whenField,
            "is", is,
            "otherFieldValues", otherFieldValues, FieldShouldWhenOther.class);

        var expectedFieldStatePredicates = new ExpectedFieldStatePredicates(fieldMetadataExtractor);

        return new FieldShouldWhenOtherCoreValidator(expectedFieldStatePredicates, field, should, fieldValues, whenField, is, otherFieldValues);
    }

    public boolean isValidValue(Object value) {
        boolean mainFieldResult = expectedFieldStatePredicates.testState(
            "field", field, should, "fieldValues", fieldValues, value);

        boolean otherFieldResult = expectedFieldStatePredicates.testState(
            "whenField", whenField, is,"otherFieldValues", otherFieldValues, value);

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

    public static void validateFieldConfiguration(String fieldByPositionName, String fieldByPositionValue,
        String isOrShouldName, ExpectedFieldState expectedFieldState,
        String otherFieldValueName, List<String> otherFieldValue, Class<? extends Annotation> annotationType) {
        if (WITHOUT_OTHER_FIELD_VALUES.contains(expectedFieldState) && CollectionUtils.isNotEmpty(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @" + annotationType.getSimpleName() + " for %s=%s for: %s=%s, field: %s should be empty",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
        }

        if (WITH_OTHER_FIELD_VALUES.contains(expectedFieldState) && CollectionUtils.isEmpty(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @" + annotationType.getSimpleName() + " for %s=%s for: %s=%s, field: %s should not be empty",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
        }

        if (FOR_STRING_AND_COLLECTION_AND_NUMBER.contains(expectedFieldState) && !canParseToNumber(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @FieldShouldWhenOther for %s=%s for: %s=%s, field: %s should have only one element with number value",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
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
