package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import org.mapstruct.Mapper;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ValidatorMetaModelMapper
    extends AdditionalPropertyMapper<ValidatorMetaModelDto, ValidatorMetaModelEntity, ValidatorMetaModel> {

    public ValidatorMetaModel toFullMetaModel(ValidatorMetaModelEntity entity) {
        return toMetaModel(entity).toBuilder()
            .realClass(ClassUtils.loadRealClass(entity.getClassName()))
            .build();
    }
}
