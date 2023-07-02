package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity;

@Entity
@Builder(toBuilder = true)
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

    @ManyToOne
    @JoinColumn(name = "translation_id")
    private TranslationEntity translationName;

    private String className;

    /**
     * when true then does it mean that this meta model is like generic enum metamodel
     */
    private Boolean isGenericEnumType;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = CLASS_META_MODEL_ID)
    private List<EnumEntryMetaModelEntity> enums;

    /**
     * When true then it means that this metamodel is used for simple, raw field like number, enum, text.
     */
    private Boolean simpleRawClass;

    @ManyToMany
    @JoinTable(
        name = "class_meta_models_generic_types",
        joinColumns = {@JoinColumn(name = CLASS_META_MODEL_ID)},
        inverseJoinColumns = {@JoinColumn(name = "generic_model_id")}
    )
    private List<ClassMetaModelEntity> genericTypes;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = CLASS_META_MODEL_ID)
    private List<FieldMetaModelEntity> fields;

    @ManyToMany
    @JoinTable(
        name = "class_validators",
        joinColumns = {@JoinColumn(name = CLASS_META_MODEL_ID)},
        inverseJoinColumns = {@JoinColumn(name = "validator_meta_model_id")}
    )
    private List<ValidatorMetaModelEntity> validators;

    @ManyToMany
    @JoinTable(
        name = "class_meta_extends_from_models",
        joinColumns = {@JoinColumn(name = CLASS_META_MODEL_ID)},
        inverseJoinColumns = {@JoinColumn(name = "extends_from_model_id")}
    )
    private List<ClassMetaModelEntity> extendsFromModels;

    @ElementCollection
    @CollectionTable(name = "class_model_additional_props",
        joinColumns = @JoinColumn(name = "class_model_id"),
        foreignKey = @ForeignKey(name = "class_models_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalPropertyEntity> additionalProperties;
}
