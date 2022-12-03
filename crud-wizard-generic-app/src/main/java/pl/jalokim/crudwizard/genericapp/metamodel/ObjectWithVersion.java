package pl.jalokim.crudwizard.genericapp.metamodel;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class ObjectWithVersion {

    private Long version;
}
