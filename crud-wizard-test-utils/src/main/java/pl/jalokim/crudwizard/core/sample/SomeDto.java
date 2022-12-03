package pl.jalokim.crudwizard.core.sample;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
public class SomeDto
    extends SomeMiddleGenericDto<SomeDto> {

    @Setter
    private SomeDto innerSomeDto;

    private Map<String, ?> someOtherMap;

    @Getter
    private Long someId;
}
