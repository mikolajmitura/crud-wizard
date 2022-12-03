package pl.jalokim.crudwizard.genericapp.rest.samples.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Table(name = "some_table_name")
@Data
public class UniqueValueSampleWithCustomTableEntity {

    @Id
    private String uuid;

    private String someFieldName;

    @Column(name = "custom_column_name")
    private String fieldWithCustomColumn;

}
