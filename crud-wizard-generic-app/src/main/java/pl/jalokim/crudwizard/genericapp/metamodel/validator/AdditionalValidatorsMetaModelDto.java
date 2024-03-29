package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class AdditionalValidatorsMetaModelDto {

    private Long id;

    @NotBlank
    @Size(min = 1, max = 250)
    private String fullPropertyPath;

    @NotEmpty
    private List<@Valid ValidatorMetaModelDto> validators;
}
