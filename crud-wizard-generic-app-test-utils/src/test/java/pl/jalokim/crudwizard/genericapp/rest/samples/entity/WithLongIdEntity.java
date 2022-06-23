package pl.jalokim.crudwizard.genericapp.rest.samples.entity;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "with_long_id_table")
public class WithLongIdEntity {

    @Id
    private Long someId;
}
