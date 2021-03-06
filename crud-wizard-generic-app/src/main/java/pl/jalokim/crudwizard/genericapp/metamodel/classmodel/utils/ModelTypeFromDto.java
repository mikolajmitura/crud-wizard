package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ModelTypeFromJavaType.getFieldTypeByNameFor;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Objects;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.utils.reflection.TypeMetadata;

@EqualsAndHashCode(callSuper = true)
@Value
class ModelTypeFromDto extends GenericModelType {

    ClassMetaModelDto comesFromClassMetaModelDto;

    public ModelTypeFromDto(ClassMetaModelDtoTempContext context,
        ClassMetaModelDto comesFromClassMetaModelDto) {
        super(context);
        this.comesFromClassMetaModelDto = comesFromClassMetaModelDto;
    }

    @Override
    public String getTypeName() {
        return Optional.ofNullable(comesFromClassMetaModelDto.getName())
            .orElseGet(comesFromClassMetaModelDto::getClassName);
    }

    @Override
    public TypeMetadata extractTypeMetadata() {
        return Optional.ofNullable(comesFromClassMetaModelDto.getClassName())
            .map(GenericModelType::typeMetadataByClassName)
            .orElse(null);
    }

    @Override
    public GenericModelType getFieldTypeByName(String fieldName, GenericModelTypeFactory genericModelTypeFactory) {
        return elements(comesFromClassMetaModelDto.getFields())
            .filter(field -> field.getFieldName().equals(fieldName))
            .map(FieldMetaModelDto::getFieldType)
            .map(classMetaModelDto -> genericModelTypeFactory.fromDto(classMetaModelDto, getContext()))
            .findFirst()
            .or(() -> elements(comesFromClassMetaModelDto.getExtendsFromModels())
                .map(classMetaModelDto -> genericModelTypeFactory.fromDto(classMetaModelDto, getContext()))
                .map(genericModelType -> genericModelType.getFieldTypeByName(fieldName, genericModelTypeFactory))
                .filter(Objects::nonNull)
                .findFirst())
            .orElseGet(() -> Optional.ofNullable(comesFromClassMetaModelDto.getClassName())
                .map(GenericModelType::typeMetadataByClassName)
                .map(typeMetadata -> getFieldTypeByNameFor(getContext(), typeMetadata, fieldName))
                .orElse(null));
    }
}
