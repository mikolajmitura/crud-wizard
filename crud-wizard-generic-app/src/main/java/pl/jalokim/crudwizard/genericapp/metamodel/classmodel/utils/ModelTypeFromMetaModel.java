package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ModelTypeFromJavaType.getFieldTypeByNameFor;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Objects;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.utils.reflection.TypeMetadata;

@EqualsAndHashCode(callSuper = true)
@Value
class ModelTypeFromMetaModel extends GenericModelType {

    ClassMetaModel comesFromMetaModel;

    public ModelTypeFromMetaModel(ClassMetaModelDtoTempContext context, ClassMetaModel comesFromMetaModel) {
        super(context);
        this.comesFromMetaModel = comesFromMetaModel;
    }

    @Override
    public String getTypeName() {
        return Optional.ofNullable(comesFromMetaModel.getName())
            .orElseGet(comesFromMetaModel::getClassName);
    }

    @Override
    public TypeMetadata extractTypeMetadata() {
        return Optional.ofNullable(comesFromMetaModel.getClassName())
            .map(GenericModelType::typeMetadataByClassName)
            .orElse(null);
    }

    @Override
    public GenericModelType getFieldTypeByName(String fieldName, GenericModelTypeFactory genericModelTypeFactory) {
        return elements(comesFromMetaModel.getFields())
            .filter(field -> field.getFieldName().equals(fieldName))
            .map(FieldMetaModel::getFieldType)
            .map(classMetaModel -> GenericModelTypeFactory.fromMetaModel(getContext(), classMetaModel))
            .findFirst()
            .or(() -> elements(comesFromMetaModel.getExtendsFromModels())
                .map(classMetaModel -> GenericModelTypeFactory.fromMetaModel(getContext(), classMetaModel))
                .map(genericModelType -> genericModelType.getFieldTypeByName(fieldName, genericModelTypeFactory))
                .filter(Objects::nonNull)
                .findFirst())
            .orElseGet(() -> Optional.ofNullable(comesFromMetaModel.getClassName())
                .map(GenericModelType::typeMetadataByClassName)
                .map(typeMetadata -> getFieldTypeByNameFor(getContext(), typeMetadata, fieldName))
                .orElse(null));
    }
}
