package pl.jalokim.crudwizard.core.sample;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class SomeDto
    extends SomeMiddleGenericDto<SomeDto> {

    @Setter
    private SomeDto innerSomeDto;

    private Map<String, ?> someOtherMap;

    @Getter
    private Long someId;
}
