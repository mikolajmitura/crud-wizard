package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ModelTypeFromJavaType.getFieldTypeByNameFor;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Objects;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity;
import pl.jalokim.utils.reflection.TypeMetadata;

@EqualsAndHashCode(callSuper = true)
@Value
class ModelTypeFromEntity extends GenericModelType {

    ClassMetaModelEntity comesFromMetaModelEntity;

    @Override
    public String getTypeName() {
        return Optional.ofNullable(comesFromMetaModelEntity.getName())
            .orElseGet(comesFromMetaModelEntity::getClassName);
    }

    @Override
    public TypeMetadata extractTypeMetadata() {
        return Optional.ofNullable(comesFromMetaModelEntity.getClassName())
            .map(GenericModelType::typeMetadataByClassName)
            .orElse(null);
    }

    @Override
    public GenericModelType getFieldTypeByName(String fieldName, GenericModelTypeFactory genericModelTypeFactory) {
        return elements(comesFromMetaModelEntity.getFields())
            .filter(field -> field.getFieldName().equals(fieldName))
            .map(FieldMetaModelEntity::getFieldType)
            .map(genericModelTypeFactory::fromEntity)
            .findFirst()
            .or(() -> elements(comesFromMetaModelEntity.getExtendsFromModels())
                .map(genericModelTypeFactory::fromEntity)
                .map(genericModelType -> genericModelType.getFieldTypeByName(fieldName, genericModelTypeFactory))
                .filter(Objects::nonNull)
                .findFirst())
            .orElseGet(() -> Optional.ofNullable(comesFromMetaModelEntity.getClassName())
                .map(GenericModelType::typeMetadataByClassName)
                .map(typeMetadata -> getFieldTypeByNameFor(typeMetadata, fieldName))
                .orElse(null));
    }
}
