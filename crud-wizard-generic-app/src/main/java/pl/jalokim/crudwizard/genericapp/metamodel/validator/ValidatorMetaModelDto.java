package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @Size(min = 3, max = 100)
    String validatorName;

    // TODO #53 implement this as well
    String validatorScript;

    Boolean parametrized;

    @Size(min = 3, max = 100)
    String namePlaceholder;

    @Size(min = 3, max = 250)
    String messagePlaceholder;
}
