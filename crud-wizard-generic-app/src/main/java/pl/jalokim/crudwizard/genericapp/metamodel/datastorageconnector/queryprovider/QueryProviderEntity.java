package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryProviderEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String className;

    private String rawQueryCode;
}
