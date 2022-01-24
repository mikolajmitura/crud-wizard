package pl.jalokim.crudwizard.core.sample;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class SuperDtoWithSuperBuilder {

    String superStringField;
    Map<String, List<Long>> someMap;
}
