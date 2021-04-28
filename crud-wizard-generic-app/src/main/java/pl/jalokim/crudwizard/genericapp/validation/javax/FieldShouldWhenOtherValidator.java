package pl.jalokim.crudwizard.genericapp.validation.javax;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getAppMessageSource;
import static pl.jalokim.crudwizard.genericapp.validation.javax.FieldShouldWhenOther.DEFAULT_MESSAGE;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.BLANK;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.CONTAINS_ALL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.CONTAINS_ANY;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EMPTY_COLLECTION;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EQUAL_TO_ANY;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.FOR_COLLECTIONS;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.EMPTY_COLLECTION_OR_NULL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.FOR_STRING;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NOT_BLANK;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NOT_NULL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NULL;
import static pl.jalokim.crudwizard.genericapp.validation.javax.OtherFieldMatch.NOT_EQUAL_TO_ALL;
import static pl.jalokim.utils.collection.CollectionUtils.intersection;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.EMPTY;
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
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.string.StringUtils;
import pl.jalokim.utils.template.TemplateAsText;

public class FieldShouldWhenOtherValidator implements BaseConstraintValidator<FieldShouldWhenOther, Object> {

    private static final String FIELD_SHOULD_BE_EXPECTED_TYPE =
        "field ${field} in class ${class} should be: ${expectedClass} class when used one of ${forExpectedClass}";

    private String field;
    private OtherFieldMatch should;
    private List<String> fieldValues;
    private String whenField;
    private OtherFieldMatch is;
    private List<String> otherFieldValues;

    private static final Map<OtherFieldMatch, BiPredicate<FieldMeta, List<String>>> VALIDATION_BY_PREDICATE = Map.of(
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
        EMPTY_COLLECTION, (fieldMeta, v) -> isNotNull(fieldMeta) && elementsToString(fieldMeta).asSet().isEmpty(),
        EMPTY_COLLECTION_OR_NULL, (fieldMeta, v) -> isNull(fieldMeta) || elementsToString(fieldMeta).asSet().isEmpty(),
        BLANK, (fieldMeta, v) -> StringUtils.isBlank(getStringText(fieldMeta)),
        NOT_BLANK, (fieldMeta, v) -> StringUtils.isNotBlank(getStringText(fieldMeta))
        // TODO tests for BLANK and NOT_BLANK
        // TODO tests for expected field as String and expected as Collection
        // TODO not needed fields like fieldValues, otherFieldValues when used OtherFieldMatch like
        // NOT_NULL, NULL, EMPTY_COLLECTION_OR_NULL, EMPTY_COLLECTION, NOT_BLANK, BLANK
    );

    @Override
    public void initialize(FieldShouldWhenOther fieldShouldWhenOther) {
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
        templateAsText.overrideVariable("collectionClass", String.class.getCanonicalName());
        templateAsText.overrideVariable("forCollections", concatElements(FOR_STRING, ", "));

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
        templateAsText.overrideVariable("collectionClass", Collection.class.getCanonicalName());
        templateAsText.overrideVariable("forCollections", concatElements(FOR_COLLECTIONS, ", "));

        throw new IllegalArgumentException(templateAsText.getCurrentTemplateText());
    }

    private static Object getFieldValue(Object targetObject, String fieldName) {
        return getValueOfField(targetObject, fieldName);
    }

    private String getValuesWhenCan(OtherFieldMatch otherFieldMatch, List<String> values) {
        if (List.of(NULL, NOT_NULL, EMPTY_COLLECTION, EMPTY_COLLECTION_OR_NULL).contains(otherFieldMatch)) {
            return EMPTY;
        }
        return concat(SPACE, elements(values).asConcatText(", "));
    }

    private static FieldMeta buildFieldMeta(Object targetClass, String fieldName) {
        return new FieldMeta(fieldName, MetadataReflectionUtils.getField(targetClass, fieldName), getFieldValue(targetClass, fieldName));
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
