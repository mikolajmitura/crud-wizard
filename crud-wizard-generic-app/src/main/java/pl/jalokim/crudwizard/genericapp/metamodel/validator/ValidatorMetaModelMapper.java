package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationMapper;
import pl.jalokim.crudwizard.genericapp.util.InstanceLoader;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@Mapper(config = MapperAsSpringBeanConfig.class,
    uses = {
        AdditionalPropertyMapper.class,
        TranslationMapper.class
    })
public abstract class ValidatorMetaModelMapper implements BaseMapper<ValidatorMetaModelDto, ValidatorMetaModelEntity, ValidatorMetaModel> {

    @Autowired
    private InstanceLoader instanceLoader;

    @Override
    @Mapping(target = "realClass", ignore = true)
    @Mapping(target = "validatorInstance", ignore = true)
    public abstract ValidatorMetaModel toMetaModel(ValidatorMetaModelEntity entity);

    public ValidatorMetaModel toFullMetaModel(ValidatorMetaModelEntity entity) {
        DataValidator<?> dataValidatorInstance = instanceLoader.createInstanceOrGetBean(entity.getClassName());

        return toMetaModel(entity).toBuilder()
            .realClass(ClassUtils.loadRealClass(entity.getClassName()))
            .validatorInstance(dataValidatorInstance)
            .messagePlaceholder(getValueOrOther(entity.getMessagePlaceholder(), dataValidatorInstance.messagePlaceholder()))
            .namePlaceholder(getValueOrOther(entity.getNamePlaceholder(), dataValidatorInstance.namePlaceholder()))
            .validatorName(getValueOrOther(entity.getValidatorName(), dataValidatorInstance.validatorName()))
            .build();
    }

    private String getValueOrOther(String nullableValue, String otherValue) {
        return Optional.ofNullable(nullableValue)
            .orElse(otherValue);
    }
}
