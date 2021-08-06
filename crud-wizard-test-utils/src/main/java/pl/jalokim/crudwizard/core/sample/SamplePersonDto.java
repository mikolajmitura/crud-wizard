package pl.jalokim.crudwizard.core.sample;

import javax.validation.constraints.NotNull;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

@Value
public class SamplePersonDto {

    @NotNull(groups = UpdateContext.class)
    Long id;
    @NotNull
    String name;
    @NotNull
    String surname;

}
