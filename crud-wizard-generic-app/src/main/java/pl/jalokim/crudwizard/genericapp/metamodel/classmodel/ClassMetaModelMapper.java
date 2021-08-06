package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.utils.ClassUtils.loadRealClass;
import static pl.jalokim.crudwizard.core.utils.NullableCollectionUtils.nullableMapToList;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ClassMetaModelMapper extends AdditionalPropertyMapper<ClassMetaModelDto, ClassMetaModelEntity, ClassMetaModel> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    public abstract ClassMetaModel toMetaModel(ClassMetaModelEntity classMetaModelEntity);

    public ClassMetaModel toSwallowDto(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModel classMetaModel = toMetaModel(classMetaModelEntity);
        classMetaModel.setGenericTypes(nullableMapToList(
            classMetaModelEntity.getGenericTypes(),
            genericType -> newClassMetaModel(genericType.getId())));

        classMetaModel.setFields(nullableMapToList(classMetaModelEntity.getFields(),
            field -> fieldMetaModelMapper.toMetaModel(metaModelContext, classMetaModel, field)));

        classMetaModel.setValidators(nullableMapToList(
            classMetaModelEntity.getValidators(),
            validator -> metaModelContext.getValidatorMetaModels()
                .getById(validator.getId())
        ));

        classMetaModel.setExtendsFromModels(nullableMapToList(
            classMetaModelEntity.getExtendsFromModels(),
            extendsFromModel -> newClassMetaModel(extendsFromModel.getId())
        ));
        classMetaModel.setRealClass(loadRealClass(classMetaModelEntity.getClassName()));

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModel(Long id) {
        return ClassMetaModel.builder()
            .id(id)
            .build();
    }
}
