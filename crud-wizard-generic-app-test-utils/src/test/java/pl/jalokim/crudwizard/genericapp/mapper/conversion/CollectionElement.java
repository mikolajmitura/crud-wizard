package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import lombok.Value;

@Value
public class CollectionElement {

    String field1;
    String field2;
    Long field3;
    NestedElementObject someObject;
}
