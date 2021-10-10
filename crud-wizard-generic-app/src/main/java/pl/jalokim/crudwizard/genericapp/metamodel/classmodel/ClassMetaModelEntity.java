package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

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
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "class_meta_models")
public class ClassMetaModelEntity extends WithAdditionalPropertiesEntity {

    public static final String CLASS_META_MODEL_ID = "class_meta_model_id";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String className;

    /**
     * When true then it means that this metamodel is used for simple, raw field like number, enum, text.
     */
    private Boolean simpleRawClass;

    @ManyToMany
    @JoinTable(
        name = "class_meta_models_generic_types",
        joinColumns = { @JoinColumn(name = CLASS_META_MODEL_ID) },
        inverseJoinColumns = { @JoinColumn(name = "generic_model_id") }
    )
    private List<ClassMetaModelEntity> genericTypes;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = CLASS_META_MODEL_ID)
    private List<FieldMetaModelEntity> fields;

    @ManyToMany
    @JoinTable(
        name = "class_validators",
        joinColumns = { @JoinColumn(name = CLASS_META_MODEL_ID) },
        inverseJoinColumns = { @JoinColumn(name = "validator_meta_model_id") }
    )
    private List<ValidatorMetaModelEntity> validators;

    @ManyToMany
    @JoinTable(
        name = "class_meta_extends_from_models",
        joinColumns = { @JoinColumn(name = CLASS_META_MODEL_ID) },
        inverseJoinColumns = { @JoinColumn(name = "extends_from_model_id") }
    )
    private List<ClassMetaModelEntity> extendsFromModels;

    public boolean shouldBeSimpleRawClass() {
        return isNotBlank(getClassName())
            && isEmpty(elements(getFields()))
            && isEmpty(elements(getGenericTypes()))
            && isEmpty(elements(getExtendsFromModels()))
            && isEmpty(elements(getValidators()));
    }
}
