package pl.jalokim.crudwizard.core.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SamplePersonDtoWitOtherObject extends SamplePersonDto {

    private SomeSimpleValueDto someOtherDto;
    private String someString;
}
