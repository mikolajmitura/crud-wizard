package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample;

import java.util.List;
import lombok.Data;

@Data
public class TwoLevelClass<D> extends SuperClass<D, List<String>> {

    private D id2;
    private String field2Level;
}
