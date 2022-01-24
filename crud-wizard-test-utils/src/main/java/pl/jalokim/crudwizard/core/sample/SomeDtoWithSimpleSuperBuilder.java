package pl.jalokim.crudwizard.core.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class SomeDtoWithSimpleSuperBuilder extends SuperDtoWithSimpleSuperBuilder {

    String someString1;
    Long someLong1;
}
