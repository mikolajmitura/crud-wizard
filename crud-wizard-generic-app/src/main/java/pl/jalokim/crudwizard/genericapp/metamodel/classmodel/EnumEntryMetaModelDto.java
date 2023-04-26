package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.NotContainsWhiteSpaces;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDto;

@Data
@EqualsAndHashCode
@Builder
public class EnumEntryMetaModelDto {

    @NotBlank
    @NotContainsWhiteSpaces
    private String name;

    @NotNull
    @Valid
    private TranslationDto translation;
}
