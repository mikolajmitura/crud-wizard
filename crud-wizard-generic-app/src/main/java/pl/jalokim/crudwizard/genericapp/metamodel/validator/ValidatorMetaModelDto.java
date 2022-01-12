package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class ValidatorMetaModelDto extends WithAdditionalPropertiesDto {

    Long id;

    @NotNull
    @ClassExists(expectedOfType = DataValidator.class)
    String className;

    String validatorName;
    String validatorScript;

    Boolean parametrized;
    String namePlaceholder;
    String messagePlaceholder;
}
