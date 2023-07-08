package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class FieldForMergeDto extends FieldMetaModelDto {

    String ownerOfFieldClassName;
}
