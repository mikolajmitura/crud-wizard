package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.ValidatorMetaModel;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.core.utils.annotations.MapperAsSpringBeanConfig;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@Mapper(config = MapperAsSpringBeanConfig.class)
public abstract class ValidatorMetaModelMapper
    extends AdditionalPropertyMapper<ValidatorMetaModelDto, ValidatorMetaModelEntity, ValidatorMetaModel> {

    @Autowired
    private ValidatorInstanceCache validatorInstanceCache;

    public ValidatorMetaModel toFullMetaModel(ValidatorMetaModelEntity entity) {
        DataValidator<?> dataValidatorInstance = validatorInstanceCache.loadInstance(entity.getClassName());

        var validatorMetaModel = toMetaModel(entity).toBuilder()
            .realClass(ClassUtils.loadRealClass(entity.getClassName()))
            .validatorInstance(dataValidatorInstance)
            .messagePlaceholder(getValueOrOther(entity.getMessagePlaceholder(), dataValidatorInstance.messagePlaceholder()))
            .namePlaceholder(getValueOrOther(entity.getNamePlaceholder(), dataValidatorInstance.namePlaceholder()))
            .validatorName(getValueOrOther(entity.getValidatorName(), dataValidatorInstance.validatorName()))
            .build();


        validatorMetaModel.getAdditionalProperties().addAll(
            elements(entity.getAdditionalProperties())
                .map(this::additionalPropertyToDto)
            .asList()
        );

        return validatorMetaModel;
    }

    private String getValueOrOther(String nullableValue, String otherValue) {
        return Optional.ofNullable(nullableValue)
            .orElse(otherValue);
    }
}
