package pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
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
public class EnumEntriesMappingEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "target_enum_by_source_enum",
        joinColumns = {@JoinColumn(name = "enum_entries_mapping_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "source_enum")
    @Column(name = "target_enum")
    Map<String, String> targetEnumBySourceEnum = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "ignored_source_enum",
        joinColumns = {@JoinColumn(name = "enum_entries_mapping_id", referencedColumnName = "id")})
    @Column(name = "value")
    List<String> ignoredSourceEnum = new ArrayList<>();

    String whenNotMappedEnum;
}
