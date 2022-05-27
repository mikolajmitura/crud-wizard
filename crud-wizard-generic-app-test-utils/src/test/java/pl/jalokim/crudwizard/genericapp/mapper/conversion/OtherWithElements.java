package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.List;
import lombok.Data;

@Data
public class OtherWithElements {

    private List<CollectionElement> elements1;
    private List<CollectionElement> elements2;
    private List<CollectionElement> elements3;
}
