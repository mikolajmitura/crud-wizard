package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.groups.FirstValidationPhase;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.AccessFieldType;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class FieldMetaModelDto extends WithAdditionalPropertiesDto {

    Long id;

    @NotNull
    @Size(min = 1, max = 100)
    String fieldName;

    @Builder.Default
    AccessFieldType accessFieldType = AccessFieldType.WRITE_READ;

    @NotNull(groups = FirstValidationPhase.class)
    ClassMetaModelDto fieldType;

    List<ValidatorMetaModelDto> validators;

}
