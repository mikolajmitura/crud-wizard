package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.utils.NullableCollectionUtils.nullableMapToList;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper.newClassMetaModel;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class FieldMetaModelMapper extends AdditionalPropertyMapper<FieldMetaModelDto, FieldMetaModelEntity, FieldMetaModel>  {

    @Override
    @Mapping(target = "fieldType", ignore = true)
    @Mapping(target = "ownerOfField", ignore = true)
    @Mapping(target = "validators", ignore = true)
    public abstract FieldMetaModel toMetaModel(FieldMetaModelEntity fieldMetaModelEntity);

    public FieldMetaModel toMetaModel(MetaModelContext metaModelContext, ClassMetaModel classMetaModel, FieldMetaModelEntity field) {
        return toMetaModel(field).toBuilder()
            .fieldType(newClassMetaModel(field.getFieldType().getId()))
            .ownerOfField(classMetaModel)
            .validators(nullableMapToList(
                field.getValidators(),
                validator -> metaModelContext.getValidatorMetaModels()
                    .getById(validator.getId())))
            .build();
    }
}
