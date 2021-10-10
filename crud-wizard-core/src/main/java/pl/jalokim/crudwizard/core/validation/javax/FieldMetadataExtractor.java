package pl.jalokim.crudwizard.core.validation.javax;

public interface FieldMetadataExtractor {

    String validatedStructureType();

    String extractOwnerTypeName(Object targetObject, String fieldName);

    Object extractValueOfField(Object targetObject, String fieldName);
}
