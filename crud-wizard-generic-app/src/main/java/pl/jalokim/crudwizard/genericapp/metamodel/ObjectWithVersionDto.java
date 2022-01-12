package pl.jalokim.crudwizard.genericapp.metamodel;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@SuperBuilder(toBuilder = true)
public class ObjectWithVersionDto {

    private Long version;
}
