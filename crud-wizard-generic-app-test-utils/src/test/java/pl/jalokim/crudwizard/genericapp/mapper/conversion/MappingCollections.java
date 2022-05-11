package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Value;

@Value
public class MappingCollections {

    List<String> strings;
    List<Long> longs;
    CollectionElement[] arrayList;
    CollectionElement[] arrayList2;
    CollectionElement[] arraySet;

    List<CollectionElement> listList;
    Set<CollectionElement> setSet;
    Map<String, CollectionElement> someMap;

    List<OtherElementCollection> mappedListElementByProvidedMethod;
    CollectionElement someOneField1;
    CollectionElement someOneField2;
}
