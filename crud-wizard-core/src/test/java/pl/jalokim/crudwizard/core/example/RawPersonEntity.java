package pl.jalokim.crudwizard.core.example;

import javax.persistence.Id;
import lombok.Data;

@Data
public class RawPersonEntity {

    @Id
    String someStringId;
}
