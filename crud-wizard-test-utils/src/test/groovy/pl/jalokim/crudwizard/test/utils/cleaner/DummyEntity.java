package pl.jalokim.crudwizard.test.utils.cleaner;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name= "dummy_entity")
public class DummyEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    Long id;
    String name;

}
