package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.MapperType;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity;

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

    private String className;

    private String beanName;

    private String methodName;

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
