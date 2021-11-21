package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity.TABLE_NAME;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Table(name = TABLE_NAME)
public class AdditionalPropertyEntity {

    public static final String TABLE_NAME = "additional_properties";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String ownerClass;
    private Long ownerId;

    private String name;
    private String valueRealClassName;
    private String rawJson;
}
