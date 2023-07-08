package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
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

    private String validatorScript;

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

    @ElementCollection
    @CollectionTable(name = "validator_additional_props",
        joinColumns = @JoinColumn(name = "validator_model_id"),
        foreignKey = @ForeignKey(name = "validator_models_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalPropertyEntity> additionalProperties;
}
