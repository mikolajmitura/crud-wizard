package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface DataStorageConnectorMetaModelRepository extends BaseRepository<DataStorageConnectorMetaModelEntity>,
    WithAdditionalPropertiesCustomRepository<DataStorageConnectorMetaModelEntity> {
}
