package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper.newClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class FieldMetaModelMapper extends AdditionalPropertyMapper<FieldMetaModelDto, FieldMetaModelEntity, FieldMetaModel> {

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

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    public abstract ClassMetaModelDto classModelToDto(ClassMetaModelEntity classMetaModelEntity);

    @Mapping(target = "version", source = "fieldMetaModelDto.version")
    @Mapping(target = "additionalProperties", source = "fieldMetaModelDto.additionalProperties")
    @Mapping(target = "id", source = "fieldMetaModelDto.id")
    @Mapping(target = "fieldType", source = "fieldType")
    @Mapping(target = "validators", ignore = true) // TODO #62 it is bug???
    public abstract FieldMetaModel toModelFromDto(FieldMetaModelDto fieldMetaModelDto,
        ClassMetaModel ownerOfField, ClassMetaModel fieldType);
}
