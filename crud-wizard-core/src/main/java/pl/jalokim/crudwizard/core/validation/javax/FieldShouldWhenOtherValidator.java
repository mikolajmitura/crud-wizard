package pl.jalokim.crudwizard.core.validation.javax;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.CONTAINS_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EMPTY_OR_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_COLLECTIONS;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.FOR_STRING_AND_COLLECTION;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_BLANK;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EMPTY;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_EQUAL_TO_ALL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITHOUT_OTHER_FIELD_VALUES;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.WITH_OTHER_FIELD_VALUES;
import static pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther.DEFAULT_MESSAGE;
import static pl.jalokim.utils.collection.CollectionUtils.intersection;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import javax.validation.ConstraintValidatorContext;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.constants.Constants;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.string.StringUtils;
import pl.jalokim.utils.template.TemplateAsText;

public class FieldShouldWhenOtherValidator implements BaseConstraintValidator<FieldShouldWhenOther, Object> {

    private static final String FIELD_SHOULD_BE_EXPECTED_TYPE =
        "field '${field}' in class ${class} should be one of class: [${expectedClasses}] when used one of ${otherFieldMatchEnums}";

    private String field;
    private ExpectedFieldState should;
    private List<String> fieldValues;
    private String whenField;
    private ExpectedFieldState is;
    private List<String> otherFieldValues;

    private static final Map<ExpectedFieldState, BiPredicate<FieldMeta, List<String>>> VALIDATION_BY_PREDICATE = Map.of(
        EQUAL_TO_ANY, (fieldMeta, v) -> v.stream()
            .map(Object::toString)
            .anyMatch(entry -> entry.equals(toStringOrNull(fieldMeta))),
        CONTAINS_ALL, (fieldMeta, v) -> elements(v).asSet().equals(elementsToString(fieldMeta).asSet()),
        CONTAINS_ANY, (fieldMeta, v) ->
            CollectionUtils.isNotEmpty(
                intersection(elements(v).asSet(), elementsToString(fieldMeta).asSet())
            ),
        NOT_EQUAL_TO_ALL, (fieldMeta, v) -> v.stream()
            .map(Object::toString)
            .noneMatch(entry -> entry.equals(toStringOrNull(fieldMeta))),
        NULL, (fieldMeta, v) -> isNull(fieldMeta),
        NOT_NULL, (fieldMeta, v) -> isNotNull(fieldMeta),
        EMPTY, (fieldMeta, v) -> isNotNull(fieldMeta) && isEmpty(fieldMeta),
        EMPTY_OR_NULL, (fieldMeta, v) -> isNull(fieldMeta) || isEmpty(fieldMeta),
        NOT_BLANK, (fieldMeta, v) -> StringUtils.isNotBlank(getStringText(fieldMeta)),
        NOT_EMPTY, (fieldMeta, v) -> isNotNull(fieldMeta) && isNotEmpty(fieldMeta)
    );

    @Override
    public void initialize(FieldShouldWhenOther fieldShouldWhenOther) {
        validateFieldConfiguration("field", fieldShouldWhenOther.field(),
            "should", fieldShouldWhenOther.should(),
            "fieldValues", Arrays.asList(fieldShouldWhenOther.fieldValues()));

        validateFieldConfiguration("whenField", fieldShouldWhenOther.whenField(),
            "is", fieldShouldWhenOther.is(),
            "otherFieldValues", Arrays.asList(fieldShouldWhenOther.otherFieldValues()));

        this.field = fieldShouldWhenOther.field();
        this.should = fieldShouldWhenOther.should();
        this.fieldValues = Arrays.asList(fieldShouldWhenOther.fieldValues());
        this.whenField = fieldShouldWhenOther.whenField();
        this.is = fieldShouldWhenOther.is();
        this.otherFieldValues = Arrays.asList(fieldShouldWhenOther.otherFieldValues());
    }

    @Override
    public boolean isValidValue(Object value, ConstraintValidatorContext context) {
        boolean mainFieldResult = VALIDATION_BY_PREDICATE.get(should)
            .test(buildFieldMeta(value, field), fieldValues);
        boolean otherFieldResult = VALIDATION_BY_PREDICATE.get(is)
            .test(buildFieldMeta(value, whenField), otherFieldValues);

        if (otherFieldResult) {
            addMessageParameter(context, "should", getAppMessageSource().getMessageByEnumWithPrefix("shouldBe", should));
            addMessageParameter(context, "fieldValues", getValuesWhenCan(should, fieldValues));
            addMessageParameter(context, "is", getAppMessageSource().getMessageByEnumWithPrefix("whenIs", is));
            addMessageParameter(context, "otherFieldValues", getValuesWhenCan(is, otherFieldValues));
            customMessage(context, DEFAULT_MESSAGE, field);
            return mainFieldResult;
        }
        return true;
    }

    private static boolean isNull(FieldMeta fieldMeta) {
        return Objects.isNull(fieldMeta.getValue());
    }

    private static boolean isNotNull(FieldMeta fieldMeta) {
        return Objects.nonNull(fieldMeta.getValue());
    }

    private static boolean isNotEmpty(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() instanceof String) {
            return StringUtils.isNotEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Collection) {
            return CollectionUtils.isNotEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Map) {
            return !((Map<?, ?>) castObject(fieldMeta)).isEmpty();
        }
        throw expectedElementsTypeOrString(fieldMeta);
    }

    private static IllegalArgumentException expectedElementsTypeOrString(FieldMeta fieldMeta) {
        TemplateAsText templateAsText = TemplateAsText.fromText(FIELD_SHOULD_BE_EXPECTED_TYPE);
        templateAsText.overrideVariable("field", fieldMeta.getFieldName());
        templateAsText.overrideVariable("class", fieldMeta.getFieldOwner().getCanonicalName());
        templateAsText.overrideVariable("expectedClasses",
            elements(String.class, Collection.class, Map.class)
                .map(Class::getCanonicalName).asConcatText(", "));
        templateAsText.overrideVariable("otherFieldMatchEnums", concatElements(FOR_STRING_AND_COLLECTION, ", "));

        return new IllegalArgumentException(templateAsText.getCurrentTemplateText());
    }

    private static boolean isEmpty(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() instanceof String) {
            return StringUtils.isEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Collection) {
            return CollectionUtils.isEmpty(castObject(fieldMeta));
        }
        if (fieldMeta.getValue() instanceof Map) {
            return ((Map<?, ?>) castObject(fieldMeta)).isEmpty();
        }
        throw expectedElementsTypeOrString(fieldMeta);
    }

    private static Object toStringOrNull(FieldMeta fieldMeta) {
        return Optional.ofNullable(fieldMeta.getValue())
            .map(Object::toString)
            .orElse(null);
    }

    public static String getStringText(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() == null) {
            return null;
        }
        if (fieldMeta.getValue() instanceof String) {
            return (String) fieldMeta.getValue();
        }
        TemplateAsText templateAsText = TemplateAsText.fromText(FIELD_SHOULD_BE_EXPECTED_TYPE);
        templateAsText.overrideVariable("field", fieldMeta.getFieldName());
        templateAsText.overrideVariable("class", fieldMeta.getFieldOwner().getCanonicalName());
        templateAsText.overrideVariable("expectedClasses", String.class.getCanonicalName());
        templateAsText.overrideVariable("otherFieldMatchEnums", concatElements(FOR_STRING, ", "));

        throw new IllegalArgumentException(templateAsText.getCurrentTemplateText());
    }

    private static Elements<String> elementsToString(FieldMeta fieldMeta) {
        if (fieldMeta.getValue() == null) {
            return Elements.empty();
        }
        if (fieldMeta.getValue() instanceof Collection) {
            return elements((Collection<?>) fieldMeta.getValue())
                .map(Object::toString);
        }
        TemplateAsText templateAsText = TemplateAsText.fromText(FIELD_SHOULD_BE_EXPECTED_TYPE);
        templateAsText.overrideVariable("field", fieldMeta.getFieldName());
        templateAsText.overrideVariable("class", fieldMeta.getFieldOwner().getCanonicalName());
        templateAsText.overrideVariable("expectedClasses", Collection.class.getCanonicalName());
        templateAsText.overrideVariable("otherFieldMatchEnums", concatElements(FOR_COLLECTIONS, ", "));

        throw new IllegalArgumentException(templateAsText.getCurrentTemplateText());
    }

    private void validateFieldConfiguration(String fieldByPositionName, String fieldByPositionValue,
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
    }

    private static Object getFieldValue(Object targetObject, String fieldName) {
        return getValueOfField(targetObject, fieldName);
    }

    private String getValuesWhenCan(ExpectedFieldState otherFieldMatch, List<String> values) {
        if (List.of(NULL, NOT_NULL, EMPTY, EMPTY_OR_NULL, NOT_EMPTY, NOT_BLANK).contains(otherFieldMatch)) {
            return Constants.EMPTY;
        }
        return concat(SPACE, elements(values).asConcatText(", "));
    }

    private static FieldMeta buildFieldMeta(Object targetClass, String fieldName) {
        return new FieldMeta(fieldName, MetadataReflectionUtils.getField(targetClass, fieldName), getFieldValue(targetClass, fieldName));
    }

    private static <T> T castObject(FieldMeta fieldMeta) {
        return (T) fieldMeta.getValue();
    }

    @Value
    public static class FieldMeta {

        String fieldName;
        Field field;
        Object value;

        public Class<?> getFieldOwner() {
            return field.getDeclaringClass();
        }
    }
}
