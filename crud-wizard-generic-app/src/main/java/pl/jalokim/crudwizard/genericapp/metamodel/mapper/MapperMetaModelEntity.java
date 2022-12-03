package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "mapper_meta_models")
public class MapperMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String mapperName;

    @Embedded
    @AttributeOverride(name = "className", column = @Column(name = "class_name"))
    @AttributeOverride(name = "beanName", column = @Column(name = "bean_name"))
    @AttributeOverride(name = "methodName", column = @Column(name = "method_name"))
    private BeanAndMethodEntity mapperBeanAndMethod;

    @Enumerated(EnumType.STRING)
    private MapperType mapperType;

    @ElementCollection
    @CollectionTable(name = "mapper_model_additional_props",
        joinColumns = @JoinColumn(name = "mapper_model_id"),
        foreignKey = @ForeignKey(name = "mapper_models_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalPropertyEntity> additionalProperties;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mapper_generation_conf_id")
    private MapperGenerateConfigurationEntity mapperGenerateConfiguration;
}
