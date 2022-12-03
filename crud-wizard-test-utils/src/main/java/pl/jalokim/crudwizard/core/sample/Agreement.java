package pl.jalokim.crudwizard.core.sample;

import java.time.LocalDate;
import lombok.Data;

@Data
public class Agreement {
    Long id;
    LocalDate signedDate;
}
