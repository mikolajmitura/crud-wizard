package pl.jalokim.crudwizard.core.sample;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@ToString
public class SomeObjectWithFewObjects {

    SomeDtoWithBuilder someDtoWithBuilder;
    SomeDtoWithSetters someDtoWithSetters;
}
