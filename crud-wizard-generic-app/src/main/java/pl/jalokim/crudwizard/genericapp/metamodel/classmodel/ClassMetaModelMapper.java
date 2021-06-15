package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ClassMetaModelMapper extends AdditionalPropertyMapper<ClassMetaModel, ClassMetaModelEntity> {

    @Autowired
    private FieldMetaModelMapper fieldMetaModelMapper;

    @Override
    @Mapping(target = "genericTypes", ignore = true)
    @Mapping(target = "fields", ignore = true)
    @Mapping(target = "validators", ignore = true)
    @Mapping(target = "extendsFromModels", ignore = true)
    public abstract ClassMetaModel toDto(ClassMetaModelEntity classMetaModelEntity);

    public ClassMetaModel toSwallowDto(MetaModelContext metaModelContext, ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModel classMetaModel = toDto(classMetaModelEntity);
        classMetaModel.setGenericTypes(mapToList(
            classMetaModelEntity.getGenericTypes(),
            genericType -> newClassMetaModel(genericType.getId())));

        classMetaModel.setFields(mapToList(classMetaModelEntity.getFields(),
            field -> fieldMetaModelMapper.toDto(metaModelContext, classMetaModel, field)));

        classMetaModel.setValidators(mapToList(
            classMetaModelEntity.getValidators(),
            validator -> metaModelContext.getValidatorMetaModels()
                .getById(validator.getId())
        ));

        classMetaModel.setExtendsFromModels(mapToList(
            classMetaModelEntity.getExtendsFromModels(),
            extendsFromModel -> newClassMetaModel(extendsFromModel.getId())
        ));

        return classMetaModel;
    }

    public static ClassMetaModel newClassMetaModel(Long id) {
        return ClassMetaModel.builder()
            .id(id)
            .build();
    }
}
