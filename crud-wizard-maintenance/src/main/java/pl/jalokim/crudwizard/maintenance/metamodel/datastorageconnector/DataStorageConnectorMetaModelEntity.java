package pl.jalokim.crudwizard.maintenance.metamodel.datastorageconnector;

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
import pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty.WithAdditionalPropertiesEntity;
import pl.jalokim.crudwizard.maintenance.metamodel.classmodel.ClassMetaModelEntity;
import pl.jalokim.crudwizard.maintenance.metamodel.datastorage.DataStorageMetaModelEntity;
import pl.jalokim.crudwizard.maintenance.metamodel.mapper.MapperMetaModelEntity;

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
    @JoinColumn(name = "mapper_meta_model_id")
    private MapperMetaModelEntity mapperMetaModel;

    @ManyToOne
    @JoinColumn(name = "class_meta_model_in_ds_id")
    private ClassMetaModelEntity classMetaModelInDataStorage;
}
