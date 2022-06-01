package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Set;
import lombok.Value;

@Value
public class ListAsListGenericType2 {

    Set<NestedCollectionElement[]> someList;

    @Value
    public static class NestedCollectionElement {

        String field1;
        String field2;
        Long field3;
        NestedElementObject someObject;
    }
}
