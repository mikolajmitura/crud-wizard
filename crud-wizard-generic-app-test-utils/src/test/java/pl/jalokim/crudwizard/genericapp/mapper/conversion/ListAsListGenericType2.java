package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Value;
import pl.jalokim.utils.collection.Elements;

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

    @Override
    public int hashCode() {
        return Objects.hash(someList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ListAsListGenericType2)) {
            return false;
        }
        ListAsListGenericType2 that = (ListAsListGenericType2) o;

        List<NestedCollectionElement> thisElements = elements(someList)
            .flatMap(Elements::elements)
            .asList();

        List<NestedCollectionElement> otherElements = elements(that.someList)
            .flatMap(Elements::elements)
            .asList();

        return Objects.equals(thisElements, otherElements);
    }
}
