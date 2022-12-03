package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.List;
import lombok.Value;

@Value
public class ListAsListGenericType1 {

    List<List<CollectionElement>> someList;
}
