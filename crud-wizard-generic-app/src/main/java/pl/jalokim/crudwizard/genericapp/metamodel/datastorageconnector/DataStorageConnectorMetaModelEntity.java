package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

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
import pl.jalokim.crudwizard.genericapp.metamodel.datastorage.DataStorageMetaModelEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider.QueryProviderEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntity;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "data_storage_connector_meta_models")
public class DataStorageConnectorMetaModelEntity extends WithAdditionalPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "data_storage_meta_model_id")
    private DataStorageMetaModelEntity dataStorageMetaModel;

    @ManyToOne
    @JoinColumn(name = "return_mapper_meta_model_id")
    private MapperMetaModelEntity mapperMetaModelForReturn;

    @ManyToOne
    @JoinColumn(name = "query_mapper_meta_model_id")
    private MapperMetaModelEntity mapperMetaModelForQuery;

    @ManyToOne
    @JoinColumn(name = "class_meta_model_in_ds_id")
    private ClassMetaModelEntity classMetaModelInDataStorage;

    private String nameOfQuery;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "query_provide_id")
    private QueryProviderEntity queryProvider;
}
