package pl.jalokim.crudwizard.core.metamodels;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
public class ApiTagMetamodel extends ObjectWithVersion {

    Long id;

    String name;
}
