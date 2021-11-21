package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.RawAdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity;

@Mapper(config = MapperAsSpringBeanConfig.class, uses = RawAdditionalPropertyMapper.class)
public abstract class ClassMetaModelMapper implements BaseMapper<ClassMetaModelDto, ClassMetaModelEntity, ClassMetaModel> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    @Mapping(target = "realClass", ignore = true)
    @Mapping(target = "enumClassMetaModel", ignore = true)
    @Mapping(target = "fieldNames", ignore = true)
    public abstract ClassMetaModel toMetaModel(ClassMetaModelEntity classMetaModelEntity);

    @Mapping(target = "simpleRawClass", ignore = true)
    public abstract ClassMetaModelEntity toEntity(ClassMetaModelDto classMetaModelDto);

    public ClassMetaModel toSwallowDto(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModel classMetaModel = toMetaModel(classMetaModelEntity);
        classMetaModel.setGenericTypes(mapToList(
            classMetaModelEntity.getGenericTypes(),
            genericType -> newClassMetaModel(genericType.getId())));

        classMetaModel.setFields(mapToList(classMetaModelEntity.getFields(),
            field -> fieldMetaModelMapper.toMetaModel(metaModelContext, classMetaModel, field)));

        classMetaModel.setValidators(mapToList(
            classMetaModelEntity.getValidators(),
            validator -> metaModelContext.getValidatorMetaModels()
                .getById(validator.getId())
        ));

        classMetaModel.setExtendsFromModels(mapToList(
            classMetaModelEntity.getExtendsFromModels(),
            extendsFromModel -> newClassMetaModel(extendsFromModel.getId())
        ));
        classMetaModel.setRealClass(loadRealClass(classMetaModelEntity.getClassName()));

        Object propertyValue = classMetaModel.getPropertyValue(ENUM_VALUES_PREFIX);
        if (propertyValue != null) {
            classMetaModel.setEnumClassMetaModel(new EnumClassMetaModel(classMetaModel));
        }

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModel(Long id) {
        return ClassMetaModel.builder()
            .id(id)
            .build();
    }
}
