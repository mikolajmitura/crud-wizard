package pl.jalokim.crudwizard.genericapp.rest.samples.entity;

import javax.persistence.Column;
import lombok.Data;

@Data
public class SomeUniqueValueEntity {

    @Column(name = "custom_column_name2")
    private String fieldWithCustomColumn2;
}
