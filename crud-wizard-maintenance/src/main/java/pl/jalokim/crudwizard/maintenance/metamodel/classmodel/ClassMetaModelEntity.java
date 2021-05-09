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
import javax.persistence.OneToMany;
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
@Table(name = "class_meta_models")
public class ClassMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String realClassName;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
        name = "class_meta_models_generic_types",
        joinColumns = { @JoinColumn(name = "class_meta_model_id") },
        inverseJoinColumns = { @JoinColumn(name = "generic_model_id") }
    )
    private List<ClassMetaModelEntity> genericTypes;

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name = "class_meta_model_id")
    private List<FieldMetaModelEntity> fields;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
        name = "meta_models_validators",
        joinColumns = { @JoinColumn(name = "class_meta_model_id") },
        inverseJoinColumns = { @JoinColumn(name = "validator_meta_model_id") }
    )
    private List<ValidatorMetaModelEntity> validators;

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
        name = "class_meta_extends_from_models",
        joinColumns = { @JoinColumn(name = "class_meta_model_id") },
        inverseJoinColumns = { @JoinColumn(name = "extends_from_model_id") }
    )
    private List<ClassMetaModelEntity> extendsFromModels;
}
