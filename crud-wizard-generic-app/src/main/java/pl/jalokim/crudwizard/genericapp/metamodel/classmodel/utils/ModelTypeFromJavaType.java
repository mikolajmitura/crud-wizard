package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import lombok.EqualsAndHashCode;
import pl.jalokim.utils.reflection.ReflectionOperationException;
import pl.jalokim.utils.reflection.TypeMetadata;

@EqualsAndHashCode(callSuper = true)
class ModelTypeFromJavaType extends GenericModelType {

    TypeMetadata comesFromJavaType;

    public ModelTypeFromJavaType(ClassMetaModelDtoTempContext context, TypeMetadata comesFromJavaType) {
        super(context);
        this.comesFromJavaType = comesFromJavaType;
    }

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
        return getFieldTypeByNameFor(getContext(), comesFromJavaType, fieldName);
    }

    public static GenericModelType getFieldTypeByNameFor(ClassMetaModelDtoTempContext context, TypeMetadata typeMetadata, String fieldName) {
        try {
            return new ModelTypeFromJavaType(context, typeMetadata.getMetaForField(fieldName));
        } catch (ReflectionOperationException ex) {
            return null;
        }
    }
}
