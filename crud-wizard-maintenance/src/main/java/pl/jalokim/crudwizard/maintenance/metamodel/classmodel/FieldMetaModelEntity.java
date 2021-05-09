package pl.jalokim.crudwizard.maintenance.metamodel.classmodel;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.maintenance.metamodel.validator.ValidatorMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "field_meta_models")
public class FieldMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fieldName;

    @ManyToOne
    @JoinColumn(name = "class_metamodel_id")
    private ClassMetaModelEntity fieldType;

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
        name = "meta_models_validators",
        joinColumns = {@JoinColumn(name = "field_meta_model_id")},
        inverseJoinColumns = {@JoinColumn(name = "validator_meta_model_id")}
    )
    private List<ValidatorMetaModelEntity> validators;
}
