package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.validation.validator.DataValidator;

@Entity
@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "validator_meta_models")
public class ValidatorMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String className;
    private String validatorName;

    /**
     * Does it mean that this validator metamodel have overridden some parameters or have additional properties set.
     */
    private Boolean parametrized;
    /**
     * below values can to overridden, when is not set then will used from
     *
     * @see DataValidator#namePlaceholder()
     */
    private String namePlaceholder;

    /**
     * When is not set then will used from
     *
     * @see DataValidator#messagePlaceholder() ()
     */
    private String messagePlaceholder;
}
