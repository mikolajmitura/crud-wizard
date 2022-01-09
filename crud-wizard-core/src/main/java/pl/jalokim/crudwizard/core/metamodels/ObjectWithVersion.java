package pl.jalokim.crudwizard.core.metamodels;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
public class ObjectWithVersion {

    private Long version;
}
