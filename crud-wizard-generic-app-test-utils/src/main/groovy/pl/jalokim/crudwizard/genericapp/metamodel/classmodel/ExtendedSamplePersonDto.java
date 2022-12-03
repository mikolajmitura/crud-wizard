package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

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
