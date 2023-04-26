package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper.newClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;

@Mapper(config = MapperAsSpringBeanConfig.class, uses = {
    AdditionalPropertyMapper.class,
    TranslationMapper.class,
    CommonClassAndFieldMapper.class
})
public abstract class FieldMetaModelMapper implements BaseMapper<FieldMetaModelDto, FieldMetaModelEntity, FieldMetaModel> {

    @Autowired
    private CommonClassAndFieldMapper commonClassAndFieldMapperInjected;

    @Override
    @Mapping(target = "fieldType", ignore = true)
    @Mapping(target = "ownerOfField", ignore = true)
    @Mapping(target = "validators", ignore = true)
    public abstract FieldMetaModel toMetaModel(FieldMetaModelEntity fieldMetaModelEntity);

    public FieldMetaModel toMetaModel(MetaModelContext metaModelContext, ClassMetaModel ownerOfField, FieldMetaModelEntity field) {
        return toMetaModel(field).toBuilder()
            .fieldType(newClassMetaModel(field.getFieldType().getId()))
            .ownerOfField(ownerOfField)
            .validators(mapToList(
                field.getValidators(),
                validator -> metaModelContext.getValidatorMetaModels()
                    .getById(validator.getId())))
            .build();
    }

    @Mapping(target = "version", source = "fieldMetaModelDto.version")
    @Mapping(target = "additionalProperties", source = "fieldMetaModelDto.additionalProperties")
    @Mapping(target = "id", source = "fieldMetaModelDto.id")
    @Mapping(target = "fieldType", source = "fieldType")
    @Mapping(target = "validators", ignore = true)
    public abstract FieldMetaModel toModelFromDto(FieldMetaModelDto fieldMetaModelDto,
        ClassMetaModel ownerOfField, ClassMetaModel fieldType);


    @Mapping(target = "ownerOfFieldClassName", source = "fieldMetaModel.ownerOfField.className")
    @Mapping(target = "fieldType", expression = ("java(mapToDto(fieldMetaModel.getFieldType(), true))"))
    public abstract FieldForMergeDto mapFieldForMerge(FieldMetaModel fieldMetaModel);

    protected ClassMetaModelDto mapToDto(ClassMetaModel classMetaModel, boolean forAvoidConflictFlag) {
        ClassMetaModelDto classMetaModelDto = commonClassAndFieldMapperInjected.mapToDto(classMetaModel);
        classMetaModelDto.setFields(new ArrayList<>(elements(classMetaModel.fetchAllFields())
            .map(this::mapFieldForMerge).asList()));
        classMetaModelDto.setExtendsFromModels(null);
        return classMetaModelDto;
    }
}
