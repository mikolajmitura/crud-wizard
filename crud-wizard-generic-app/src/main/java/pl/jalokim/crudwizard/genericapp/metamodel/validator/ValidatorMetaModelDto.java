package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ValidatorMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    @NotNull
    @ClassExists(expectedOfType = DataValidator.class)
    String className;

    String validatorName;

    Boolean parametrized;
    String namePlaceholder;
    String messagePlaceholder;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
