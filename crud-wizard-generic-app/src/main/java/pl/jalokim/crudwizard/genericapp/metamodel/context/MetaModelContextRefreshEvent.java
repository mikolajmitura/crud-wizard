package pl.jalokim.crudwizard.genericapp.metamodel.context;

import java.time.OffsetDateTime;
import lombok.Value;

@Value
public class MetaModelContextRefreshEvent {

    String reason;
    OffsetDateTime refreshDateTime;
}
