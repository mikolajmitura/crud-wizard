package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
}
