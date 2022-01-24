package pl.jalokim.crudwizard.core.sample;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class SuperDtoWithSimpleSuperBuilder {

    String superStringField;
    LocalDateTime localDateTime1;
}
