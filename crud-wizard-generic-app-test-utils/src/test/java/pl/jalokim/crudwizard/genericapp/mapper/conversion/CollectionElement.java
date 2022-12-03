package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import lombok.Value;

@Value
public class CollectionElement {

    String field1;
    String field2;
    Long field3;
    NestedElementObject someObject;

    @Override
    public String toString() {
        return "CollectionElement{" +
            "field1='" + field1 + '\'' + "\n" +
            ", field2='" + field2 + '\'' + "\n" +
            ", field3=" + field3 + "\n" +
            ", someObject=" + someObject + "\n" +
            '}';
    }
}
