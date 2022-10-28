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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "mapper_generation_conf")
public class MapperGenerateConfigurationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private boolean globalEnableAutoMapping;
    private boolean globalIgnoreMappingProblems;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "field_resolver_for_target_id")
    private FieldMetaResolverConfigurationEntity fieldMetaResolverForRawTarget;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "field_resolver_for_source_id")
    private FieldMetaResolverConfigurationEntity fieldMetaResolverForRawSource;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_configuration_id")
    private MapperConfigurationEntity rootConfiguration;

    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "sub_mappers_as_methods_id")
    private List<MapperConfigurationEntity> subMappersAsMethods;


}
