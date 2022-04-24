package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.ScriptLanguage;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto;

@Value
@Builder
public class MapperScriptDto {

    @NotNull
    @Valid
    ClassMetaModelDto sourceMetaModel;

    @NotNull
    @Valid
    ClassMetaModelDto targetMetaModel;

    @NotEmpty
    String scriptCode;

    @NotNull
    ScriptLanguage language;
}
