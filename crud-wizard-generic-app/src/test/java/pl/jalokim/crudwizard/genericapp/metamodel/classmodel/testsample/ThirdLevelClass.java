package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThirdLevelClass extends TwoLevelClass<Long> {

    String id3;
    String value3;
}
