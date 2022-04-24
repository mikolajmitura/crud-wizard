package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "mapper_configuration")
public class MapperConfigurationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Mapper name or method name
     */
    private String name;

    @ManyToOne
    @JoinColumn(name = "source_meta_model_id")
    private ClassMetaModelEntity sourceMetaModel;

    @ManyToOne
    @JoinColumn(name = "target_meta_model_id")
    private ClassMetaModelEntity targetMetaModel;

    private Boolean enableAutoMapping;

    private Boolean ignoreMappingProblems;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "propert_overridden_mapping_id")
    private List<PropertiesOverriddenMappingEntity> propertyOverriddenMapping;
}
