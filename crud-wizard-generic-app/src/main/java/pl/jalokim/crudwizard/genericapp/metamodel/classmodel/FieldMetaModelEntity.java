package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

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
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalProperty;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"fieldType"})
@Table(name = "field_meta_models")
public class FieldMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fieldName;

    @ManyToOne
    @JoinColumn(name = "class_metamodel_id")
    private ClassMetaModelEntity fieldType;

    @ManyToMany
    @JoinTable(
        name = "field_validators",
        joinColumns = {@JoinColumn(name = "field_meta_model_id")},
        inverseJoinColumns = {@JoinColumn(name = "validator_meta_model_id")}
    )
    private List<ValidatorMetaModelEntity> validators;

    @ElementCollection
    @CollectionTable(name = "field_additional_props",
        joinColumns = @JoinColumn(name = "field_model_id"),
        foreignKey = @ForeignKey(name = "fields_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalProperty> additionalProperties;
}
