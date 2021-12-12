package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.time.LocalDateTime;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

public class ExtendedSamplePersonDto extends SamplePersonDto {

    private String id;
    private Long someNumber;
    private LocalDateTime someDateTimeField;
}
