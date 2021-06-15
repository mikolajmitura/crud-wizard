package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper.newClassMetaModel;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class FieldMetaModelMapper extends AdditionalPropertyMapper<FieldMetaModel, FieldMetaModelEntity>  {

    @Override
    @Mapping(target = "fieldType", ignore = true)
    @Mapping(target = "ownerOfField", ignore = true)
    @Mapping(target = "validators", ignore = true)
    public abstract FieldMetaModel toDto(FieldMetaModelEntity fieldMetaModelEntity);

    public FieldMetaModel toDto(MetaModelContext metaModelContext, ClassMetaModel classMetaModel, FieldMetaModelEntity field) {
        return toDto(field).toBuilder()
            .fieldType(newClassMetaModel(field.getFieldType().getId()))
            .ownerOfField(classMetaModel)
            .validators(mapToList(
                field.getValidators(),
                validator -> metaModelContext.getValidatorMetaModels()
                    .getById(validator.getId())))
            .build();
    }
}
