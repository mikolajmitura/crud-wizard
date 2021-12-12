package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.utils.reflection.ReflectionOperationException;
import pl.jalokim.utils.reflection.TypeMetadata;

@EqualsAndHashCode(callSuper = true)
@Value
class ModelTypeFromJavaType extends GenericModelType {

    TypeMetadata comesFromJavaType;

    @Override
    public String getTypeName() {
        return comesFromJavaType.getRawType().getCanonicalName();
    }

    @Override
    public TypeMetadata extractTypeMetadata() {
        return comesFromJavaType;
    }

    @Override
    public GenericModelType getFieldTypeByName(String fieldName, GenericModelTypeFactory genericModelTypeFactory) {
        return getFieldTypeByNameFor(comesFromJavaType, fieldName);
    }

    public static GenericModelType getFieldTypeByNameFor(TypeMetadata typeMetadata, String fieldName) {
        try {
            return new ModelTypeFromJavaType(typeMetadata.getMetaForField(fieldName));
        } catch (ReflectionOperationException ex) {
            return null;
        }
    }
}
