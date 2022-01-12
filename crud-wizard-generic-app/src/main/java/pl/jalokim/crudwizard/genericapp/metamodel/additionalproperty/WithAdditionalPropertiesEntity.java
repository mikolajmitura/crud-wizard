package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import java.util.List;
import javax.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;

@Getter
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Setter
public abstract class WithAdditionalPropertiesEntity extends BaseEntity {

    public abstract List<AdditionalProperty> getAdditionalProperties();
}
