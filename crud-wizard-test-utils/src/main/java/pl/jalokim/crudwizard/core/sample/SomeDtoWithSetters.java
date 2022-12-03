package pl.jalokim.crudwizard.core.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SomeDtoWithSetters extends SamplePersonDto {

    private String someString2;
    private Long someLong2;
}
