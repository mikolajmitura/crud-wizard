package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig.rawJsonToObject;
import static pl.jalokim.crudwizard.core.metamodels.EnumClassMetaModel.ENUM_VALUES_PREFIX;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import javax.validation.ConstraintValidatorContext;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.utils.string.StringUtils;

public class EnumValuesInAdditionalPropertiesValidator
    implements BaseConstraintValidator<EnumValuesInAdditionalProperties, ClassMetaModelDto> {

    @Override
    public boolean isValidValue(ClassMetaModelDto classMetaModelDto, ConstraintValidatorContext context) {
        if (classMetaModelDto.getIsGenericEnumType() != null && classMetaModelDto.getIsGenericEnumType()) {
            AdditionalPropertyDto enumValuesProperty = classMetaModelDto.getProperty(ENUM_VALUES_PREFIX);
            if (!enumValuesProperty.getValueRealClassName().equals(String[].class.getCanonicalName())) {
                customMessage(context, "{EnumValuesInAdditionalProperties.invalid.enumvalues.class}");
                return false;
            }
            String[] enumValuesArray = rawJsonToObject(enumValuesProperty.getRawJson(), enumValuesProperty.getValueRealClassName());

            if (enumValuesArray.length < 1) {
                customMessage(context, "{EnumValuesInAdditionalProperties.invalid.enumvalues.invalidSize}");
                return false;
            }
            boolean allValid = true;

            for (int index = 0; index < enumValuesArray.length; index++) {
                String enumValue = enumValuesArray[index];
                if (StringUtils.isBlank(enumValue) || enumValue.contains(" ") || enumValue.contains("\t")) {
                    allValid = false;
                    customMessage(context, createMessagePlaceholder("EnumValuesInAdditionalProperties.invalid.at.index", index));
                }
            }
            return allValid;
        }
        return true;
    }
}
