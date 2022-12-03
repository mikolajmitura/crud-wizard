package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import lombok.Data;
import lombok.Value;

@Value
public class ObjectWithEnum {
    ExampleEnum2 someEnum;
    OtherObjectWithEnum otherObjectWithEnum;

    @Data
    public static class OtherObjectWithEnum {
        ExampleEnum2 otherEnum;
    }
}
