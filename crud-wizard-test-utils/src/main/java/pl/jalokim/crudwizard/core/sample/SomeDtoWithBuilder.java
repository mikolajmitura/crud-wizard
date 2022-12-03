package pl.jalokim.crudwizard.core.sample;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@Data
public class SomeDtoWithBuilder extends SomeDto {

    String test1;
    Long testLong1;
    LocalDateTime localDateTime1;
}
