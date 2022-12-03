package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.time.LocalDate;
import lombok.Data;

@Data
public class SomeDocument1Entity {

    private Long id;
    private Long number;
    private LocalDate someLocalDate;
}
