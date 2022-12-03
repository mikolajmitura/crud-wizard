package pl.jalokim.crudwizard.core.sample;

import lombok.Value;

@Value
public class ClassHasSamplePersonDto {

    SamplePersonDto samplePersonDto;
    SamplePersonDto otherPersonDto;
    SomeObjectWithFewObjects someObjectWithFewObjects;
    Long someId;
}
