package pl.jalokim.crudwizard.core.validation.javax.inner;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState;

@Value
public class DependValuesMeta {

    List<String> values;
    String fieldByPositionName;
    ExpectedFieldState expectedFieldStateFieldName;
    String otherFieldValuesName;
}
