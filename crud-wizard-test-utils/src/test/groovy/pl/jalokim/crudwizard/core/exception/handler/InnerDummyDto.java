package pl.jalokim.crudwizard.core.exception.handler;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InnerDummyDto {

    List<InnerDummyDto> anotherList;

    String someText;

    Long someLong;

    InnerDummyDto innerDummyDto1;
    InnerDummyDto innerDummyDto2;
}
