package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.time.LocalDate;
import lombok.Value;

@Value
public class SomeDocument1 {

    Long id;
    Long number;
    LocalDate validToDate;
    String fromParentField;
}
