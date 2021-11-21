package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.wrapAsExternalPlaceholder;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_COLLECTIONS;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING_AND_COLLECTION;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING_AND_COLLECTION_AND_NUMBER;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MAX;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.MIN;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EQUAL_TO_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITHOUT_OTHER_FIELD_VALUES;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITH_OTHER_FIELD_VALUES;
import static pl.jalokim.utils.collection.CollectionUtils.intersection;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.tuple.Pair;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.constants.Constants;
import pl.jalokim.utils.string.StringUtils;
import pl.jalokim.utils.template.TemplateAsText;

@RequiredArgsConstructor
@Getter
@SuppressWarnings({"PMD.GodClass", "PMD.NPathComplexity"})
public class FieldShouldWhenOtherCoreValidator {

    private static final String FIELD_SHOULD_BE_EXPECTED_TYPE =
        "field '${field}' in ${structure_type}: ${owner_type_name} should be one of class: [${expectedClasses}] when used one of ${otherFieldMatchEnums}";

    private final Map<ExpectedFieldState, BiPredicate<FieldMeta, DependValuesMeta>> validationByPredicate = Map.ofEntries(
        entry(EQUAL_TO_ANY, (fieldMeta, v) -> v.getValues().stream()
            .map(Object::toString)
            .anyMatch(entry -> entry.equals(toStringOrNull(fieldMeta)))),
        entry(CONTAINS_ALL, (fieldMeta, v) -> elements(v.getValues()).asSet().equals(elementsToString(fieldMeta).asSet())),
        entry(CONTAINS_ANY, (fieldMeta, v) ->
            CollectionUtils.isNotEmpty(
                intersection(elements(v.getValues()).asSet(), elementsToString(fieldMeta).asSet())
            )),
        entry(NOT_EQUAL_TO_ALL, (fieldMeta, v) -> v.getValues().stream()
            .map(Object::toString)
            .noneMatch(entry -> entry.equals(toStringOrNull(fieldMeta)))),
        entry(NULL, (fieldMeta, v) -> isNull(fieldMeta)),
        entry(NOT_NULL, (fieldMeta, v) -> isNotNull(fieldMeta)),
        entry(EMPTY, (fieldMeta, v) -> isNotNull(fieldMeta) && isEmpty(fieldMeta)),
        entry(EMPTY_OR_NULL, (fieldMeta, v) -> isNull(fieldMeta) || isEmpty(fieldMeta)),
        entry(NOT_BLANK, (fieldMeta, v) -> StringUtils.isNotBlank(getStringText(fieldMeta))),
        entry(NOT_EMPTY, (fieldMeta, v) -> isNotNull(fieldMeta) && isNotEmpty(fieldMeta)),
        entry(MAX, this::hasMaxValue),
        entry(MIN, this::hasMinValue)
    );

    private final FieldMetadataExtractor fieldMetadataExtractor;
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
            "fieldValues", fieldValues);

        validateFieldConfiguration("whenField", whenField,
            "is", is,
            "otherFieldValues", otherFieldValues);

        return new FieldShouldWhenOtherCoreValidator(fieldMetadataExtractor, field, should, fieldValues, whenField, is, otherFieldValues);
    }

    public boolean isValidValue(Object value) {
        boolean mainFieldResult = validationByPredicate.get(should)
            .test(buildFieldMeta(value, field),
                new DependValuesMeta(fieldValues, "field", should, "fieldValues"));

        boolean otherFieldResult = validationByPredicate.get(is)
            .test(buildFieldMeta(value, whenField),
                new DependValuesMeta(otherFieldValues, "whenField", is, "otherFieldValues"));

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

    private static boolean isNull(FieldMeta fieldMeta) {
        return Objects.isNull(fieldMeta.getValue());
    }

    private static boolean isNotNull(FieldMeta fieldMeta) {
        return Objects.nonNull(fieldMeta.getValue());
    }

    private boolean isNotEmpty(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() instanceof String) {
            return StringUtils.isNotEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Collection) {
            Collection<?> collection = castObject(fieldMeta);
            return CollectionUtils.isNotEmpty(collection);
        }
        if (fieldMeta.getValue() instanceof Map) {
            return !((Map<?, ?>) castObject(fieldMeta)).isEmpty();
        }
        throw expectedElementsTypeOrString(fieldMeta);
    }

    private IllegalArgumentException expectedElementsTypeOrString(FieldMeta fieldMeta) {
        return expectedElementsOfSomeTypeException(fieldMeta,
            List.of(String.class, Collection.class, Map.class),
            FOR_STRING_AND_COLLECTION);
    }

    private IllegalArgumentException expectedElementsOfSomeTypeException(FieldMeta fieldMeta,
        List<Class<?>> expectedClasses, List<ExpectedFieldState> otherFieldMatchEnums) {
        TemplateAsText templateAsText = TemplateAsText.fromText(FIELD_SHOULD_BE_EXPECTED_TYPE)
            .overrideVariable("field", fieldMeta.getFieldName())
            .overrideVariable("structure_type", fieldMetadataExtractor.validatedStructureType())
            .overrideVariable("owner_type_name", fieldMeta.getOwnerTypeName())
            .overrideVariable("expectedClasses",
                elements(expectedClasses)
                    .map(Class::getCanonicalName).asConcatText(", "))
            .overrideVariable("otherFieldMatchEnums", concatElements(otherFieldMatchEnums, ", "));

        return new IllegalArgumentException(templateAsText.getCurrentTemplateText());
    }

    private boolean isEmpty(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() instanceof String) {
            return StringUtils.isEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Collection) {
            Collection<?> collection = castObject(fieldMeta);
            return CollectionUtils.isEmpty(collection);
        }
        if (fieldMeta.getValue() instanceof Map) {
            return ((Map<?, ?>) castObject(fieldMeta)).isEmpty();
        }
        throw expectedElementsTypeOrString(fieldMeta);
    }

    private boolean hasMaxValue(FieldMeta fieldMeta, DependValuesMeta dependValuesMeta) {
        if (fieldMeta.getValue() == null) {
            return false;
        }

        if (fieldMeta.getValue() instanceof String) {
            String textValue = castObject(fieldMeta);
            return textValue.length() <= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Collection) {
            Collection<?> collection = castObject(fieldMeta);
            return collection.size() <= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Map) {
            Map<?, ?> map = castObject(fieldMeta);
            return map.size() <= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Integer
            || fieldMeta.getValue() instanceof Long
            || fieldMeta.getValue() instanceof Byte
            || fieldMeta.getValue() instanceof BigInteger
            || fieldMeta.getValue() instanceof Short) {
            return new BigInteger(fieldMeta.getValue().toString())
                .compareTo(parseToBigIntegerNumber(fieldMeta, dependValuesMeta)) <= 0;
        }
        if (fieldMeta.getValue() instanceof Double
            || fieldMeta.getValue() instanceof Float
            || fieldMeta.getValue() instanceof BigDecimal) {
            return new BigDecimal(fieldMeta.getValue().toString())
                .compareTo(parseToNumber(dependValuesMeta.getValues())) <= 0;
        }
        throw expectedElementsOfSomeTypeException(fieldMeta,
            List.of(Collection.class, Map.class, String.class, Number.class),
            FOR_STRING_AND_COLLECTION_AND_NUMBER);
    }

    private boolean hasMinValue(FieldMeta fieldMeta, DependValuesMeta dependValuesMeta) {
        if (fieldMeta.getValue() == null) {
            return false;
        }

        if (fieldMeta.getValue() instanceof String) {
            String textValue = castObject(fieldMeta);
            return textValue.length() >= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Collection) {
            Collection<?> collection = castObject(fieldMeta);
            return collection.size() >= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Map) {
            Map<?, ?> map = castObject(fieldMeta);
            return map.size() >= parseToBigIntegerNumber(fieldMeta, dependValuesMeta).longValue();
        }
        if (fieldMeta.getValue() instanceof Integer
            || fieldMeta.getValue() instanceof Long
            || fieldMeta.getValue() instanceof Byte
            || fieldMeta.getValue() instanceof BigInteger
            || fieldMeta.getValue() instanceof Short) {
            return new BigInteger(fieldMeta.getValue().toString())
                .compareTo(parseToBigIntegerNumber(fieldMeta, dependValuesMeta)) >= 0;
        }
        if (fieldMeta.getValue() instanceof Double
            || fieldMeta.getValue() instanceof Float
            || fieldMeta.getValue() instanceof BigDecimal) {
            return new BigDecimal(fieldMeta.getValue().toString())
                .compareTo(parseToNumber(dependValuesMeta.getValues())) >= 0;
        }
        throw expectedElementsOfSomeTypeException(fieldMeta,
            List.of(Collection.class, Map.class, String.class, Number.class),
            FOR_STRING_AND_COLLECTION_AND_NUMBER);
    }

    private static Object toStringOrNull(FieldMeta fieldMeta) {
        return Optional.ofNullable(fieldMeta.getValue())
            .map(Object::toString)
            .orElse(null);
    }

    public String getStringText(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() == null) {
            return null;
        }
        if (fieldMeta.getValue() instanceof String) {
            return (String) fieldMeta.getValue();
        }
        throw expectedElementsOfSomeTypeException(fieldMeta, List.of(String.class), FOR_STRING);
    }

    private Elements<String> elementsToString(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() == null) {
            return Elements.empty();
        }
        if (fieldMeta.getValue() instanceof Collection) {
            return elements((Collection<?>) fieldMeta.getValue())
                .map(Object::toString);
        }
        throw expectedElementsOfSomeTypeException(fieldMeta, List.of(Collection.class), FOR_COLLECTIONS);
    }

    private static void validateFieldConfiguration(String fieldByPositionName, String fieldByPositionValue,
        String isOrShouldName, ExpectedFieldState expectedFieldState,
        String otherFieldValueName, List<String> otherFieldValue) {
        if (WITHOUT_OTHER_FIELD_VALUES.contains(expectedFieldState) && CollectionUtils.isNotEmpty(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @FieldShouldWhenOther for %s=%s for: %s=%s, field: %s should be empty",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
        }

        if (WITH_OTHER_FIELD_VALUES.contains(expectedFieldState) && CollectionUtils.isEmpty(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @FieldShouldWhenOther for %s=%s for: %s=%s, field: %s should not be empty",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
        }

        if (FOR_STRING_AND_COLLECTION_AND_NUMBER.contains(expectedFieldState) && !canParseToNumber(otherFieldValue)) {
            throw new IllegalArgumentException(String.format(
                "invalid @FieldShouldWhenOther for %s=%s for: %s=%s, field: %s should have only one element with number value",
                fieldByPositionName, fieldByPositionValue, isOrShouldName, expectedFieldState, otherFieldValueName));
        }
    }

    private String getValuesWhenCan(ExpectedFieldState otherFieldMatch, List<String> values) {
        if (List.of(NULL, NOT_NULL, EMPTY, EMPTY_OR_NULL, NOT_EMPTY, NOT_BLANK).contains(otherFieldMatch)) {
            return Constants.EMPTY;
        }
        return concat(SPACE, elements(values).asConcatText(", "));
    }

    private FieldMeta buildFieldMeta(Object targetObject, String fieldName) {
        return new FieldMeta(fieldName,
            fieldMetadataExtractor.extractValueOfField(targetObject, fieldName),
            fieldMetadataExtractor.extractOwnerTypeName(targetObject, fieldName));
    }

    private static <T> T castObject(FieldMeta fieldMeta) {
        return (T) fieldMeta.getValue();
    }

    @Value
    public static class FieldMeta {

        String fieldName;
        Object value;
        String ownerTypeName;
    }

    @Value
    public static class DependValuesMeta {

        List<String> values;
        String fieldByPositionName;
        ExpectedFieldState expectedFieldStateFieldName;
        String otherFieldValuesName;
    }

    private static Pair<ExpectedFieldState, BiPredicate<FieldMeta, DependValuesMeta>> entry(
        ExpectedFieldState expectedFieldState,
        BiPredicate<FieldMeta, DependValuesMeta> validationRule) {
        return Pair.of(expectedFieldState, validationRule);
    }

    private static BigDecimal parseToNumber(List<String> numberValue) {
        if (numberValue == null || numberValue.size() != 1) {
            throw new NumberFormatException();
        }
        return new BigDecimal(numberValue.get(0));
    }

    private static BigInteger parseToBigIntegerNumber(FieldMeta fieldMeta, DependValuesMeta dependValuesMeta) {
        try {
            return new BigInteger(dependValuesMeta.getValues().get(0));
        } catch (NumberFormatException ex) {
            throw invalidFieldShouldWhenOtherConfig(fieldMeta, dependValuesMeta,
                String.format("value of field: %s should be not floating point number", dependValuesMeta.getOtherFieldValuesName()), ex);
        }
    }

    private static boolean canParseToNumber(List<String> numberValue) {
        try {
            parseToNumber(numberValue);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static IllegalArgumentException invalidFieldShouldWhenOtherConfig(FieldMeta fieldMeta,
        DependValuesMeta dependValuesMeta, String restOfMessage, Exception causeException) {

        return new IllegalArgumentException(
            String.format("invalid @FieldShouldWhenOther for %s=%s for: %s=%s, %s",
                dependValuesMeta.getFieldByPositionName(), fieldMeta.getFieldName(),
                dependValuesMeta.getOtherFieldValuesName(), dependValuesMeta.getValues(),
                restOfMessage),
            causeException
        );
    }
}