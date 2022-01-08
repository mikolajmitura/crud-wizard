package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

@EqualsAndHashCode(callSuper = true)
public class ExtendedSamplePersonDto extends SamplePersonDto {

    @SuppressWarnings("PMD.UnusedPrivateField")
    private String id;

    @Getter
    @Setter
    private Long someNumber;

    @Getter
    @Setter
    private LocalDateTime someDateTimeField;
}
