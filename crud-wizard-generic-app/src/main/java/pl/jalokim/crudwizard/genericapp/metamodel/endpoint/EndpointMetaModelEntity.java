package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import java.util.List;
import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
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
import org.springframework.http.HttpMethod;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.joinresults.DataStorageResultsJoinerEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.service.ServiceMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.AdditionalValidatorsEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "endpoint_meta_models")
public class EndpointMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "api_tag_id")
    private ApiTagEntity apiTag;

    private String baseUrl;

    @Enumerated(EnumType.STRING)
    private HttpMethod httpMethod;

    private String operationName;

    @ManyToOne
    @JoinColumn(name = "payload_metamodel_id")
    private ClassMetaModelEntity payloadMetamodel;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "endpoint_id")
    private List<AdditionalValidatorsEntity> payloadMetamodelAdditionalValidators;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "endpoint_meta_model_id")
    private ClassMetaModelEntity queryArguments;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "path_params_metamodel_id")
    private ClassMetaModelEntity pathParams;

    @ManyToOne
    @JoinColumn(name = "service_meta_model_id")
    private ServiceMetaModelEntity serviceMetaModel;

    private Boolean invokeValidation;

    @ManyToOne
    @JoinColumn(name = "response_meta_model_id")
    private EndpointResponseMetaModelEntity responseMetaModel;

    @OneToMany
    @JoinColumn(name = "endpoint_id")
    private List<DataStorageConnectorMetaModelEntity> dataStorageConnectors;

    @OneToMany(cascade = {CascadeType.ALL})
    @JoinColumn(name = "endpoint_id")
    private List<DataStorageResultsJoinerEntity> dataStorageResultsJoiners;

    @ElementCollection
    @CollectionTable(name = "endpoint_additional_props",
        joinColumns = @JoinColumn(name = "endpoint_model_id"),
        foreignKey = @ForeignKey(name = "endpoint_models_properties_fk"))
    @AttributeOverride(name = "name", column = @Column(name = "name"))
    @AttributeOverride(name = "valueRealClassName", column = @Column(name = "valueRealClassName"))
    @AttributeOverride(name = "rawJson", column = @Column(name = "rawJson"))
    private List<AdditionalPropertyEntity> additionalProperties;
}
