package pl.jalokim.crudwizard.core.validation.javax.inner;

import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;

import java.lang.reflect.Field;
import pl.jalokim.crudwizard.core.validation.javax.FieldMetadataExtractor;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

public class FieldMetadataExtractorFromClass implements FieldMetadataExtractor  {

    public static final FieldMetadataExtractorFromClass FIELD_META_EXTRACTOR_FROM_CLASS = new FieldMetadataExtractorFromClass();

    @Override
    public String validatedStructureType() {
        return "class";
    }

    @Override
    public String extractOwnerTypeName(Object targetObject, String fieldName) {
        Field field = MetadataReflectionUtils.getField(targetObject, fieldName);
        return field.getDeclaringClass().getCanonicalName();
    }

    @Override
    public Object extractValueOfField(Object targetObject, String fieldName) {
        return getValueOfField(targetObject, fieldName);
    }

}
