package pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty;

import java.util.List;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.crudwizard.maintenance.metamodel.BaseEntity;

@Getter
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Setter
public abstract class WithAdditionalPropertiesEntity extends BaseEntity {

    @Transient
    private List<AdditionalPropertyEntity> additionalProperties;

    public List<AdditionalPropertyEntity> getAdditionalProperties() {
        if (additionalProperties == null) {
            additionalProperties = AdditionalPropertyService.getInstance()
                .fetchAdditionalPropertiesFor(this);
        }
        return additionalProperties;
    }

    public abstract Long getId();
}
