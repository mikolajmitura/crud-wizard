package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalProperty;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryProviderEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String className;

    private String rawQueryCode;

    @ElementCollection
    @CollectionTable(name = "query_prov_additional_props",
        joinColumns = @JoinColumn(name = "query_provider_model_id"),
        foreignKey = @ForeignKey(name = "query_providers_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalProperty> additionalProperties;
}
