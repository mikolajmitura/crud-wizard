package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@Mapper(config = MapperAsSpringBeanConfig.class,
    uses = {
        AdditionalPropertyMapper.class,
        TranslationMapper.class,
})
public interface CommonClassAndFieldMapper {

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    @Mapping(target = "isGenericEnumType", expression = ("java(classMetaModel.isGenericMetamodelEnum())"))
    ClassMetaModelDto mapToDto(ClassMetaModel classMetaModel);

    @Mapping(target = "classMetaModelDtoType", ignore = true)
    @Mapping(target = "enumMetaModel", source = "classMetaModelEntity")
    ClassMetaModelDto classModelToDto(ClassMetaModelEntity classMetaModelEntity);

    @Mapping(target = "parametrized", ignore = true)
    @Mapping(target = "className", ignore = true)
    ValidatorMetaModelDto mapValidatorToDto(ValidatorMetaModel validatorMetaModel);

    @Mapping(target = "simpleRawClass", ignore = true)
    @Mapping(target = "enums", source = "enumMetaModel.enums")
    ClassMetaModelEntity toEntity(ClassMetaModelDto classMetaModelDto);

    @Mapping(target = "id", ignore = true)
    EnumEntryMetaModelEntity mapEnumEntryToEntity(EnumEntryMetaModelDto enumEntryMetaModelDto);
}
