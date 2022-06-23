package pl.jalokim.crudwizard.core.example;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Table(name = "some_name_of_table")
@Data
public class OtherPersonEntity {

    @Id
    @Column(name = "id_raw_name")
    Long idAsLong;
}
