package pl.jalokim.crudwizard.genericapp.rest.samples.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SomeDtoWithNestedFields extends SuperClassDto {

    String uuid;
    Long refId;
    String referenceNumber;
    LocalDate createdDate;
    SomeRawDto otherField;
    NestedObject2L level2;
    NestedObject2L level22;
}
