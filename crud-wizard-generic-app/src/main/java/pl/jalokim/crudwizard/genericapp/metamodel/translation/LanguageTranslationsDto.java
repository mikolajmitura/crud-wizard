package pl.jalokim.crudwizard.genericapp.metamodel.translation;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.validation.LanguageCanBeEnabled;

@Value
@Builder
@LanguageCanBeEnabled
public class LanguageTranslationsDto {

    @NotBlank
    String languageCode;

    @NotBlank
    String languageFullName;

    @NotNull
    Boolean enabled;

    @Valid
    Map<@NotNull String, @NotNull String> translations;
}
