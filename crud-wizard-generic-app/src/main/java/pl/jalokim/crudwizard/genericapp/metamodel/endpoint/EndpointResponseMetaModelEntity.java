package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

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
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "endpoint_response_meta_models")
public class EndpointResponseMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reponse_metamodel_id")
    private ClassMetaModelEntity classMetaModel;

    @ManyToOne
    @JoinColumn(name = "reponse_mapper_id")
    private MapperMetaModelEntity mapperMetaModel;

    private Long successHttpCode;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "query_provide_id")
    private QueryProviderEntity queryProvider;

    @ElementCollection
    @CollectionTable(name = "endpoint_resp_additional_props",
        joinColumns = @JoinColumn(name = "endpoint_resp_id"),
        foreignKey = @ForeignKey(name = "endpoint_resp_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalProperty> additionalProperties;
}
