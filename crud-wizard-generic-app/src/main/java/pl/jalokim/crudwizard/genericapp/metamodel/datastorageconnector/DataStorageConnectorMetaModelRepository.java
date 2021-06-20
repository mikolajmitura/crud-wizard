package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import pl.jalokim.crudwizard.core.jpa.BaseRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

public interface DataStorageConnectorMetaModelRepository extends BaseRepository<DataStorageConnectorMetaModelEntity>,
    WithAdditionalPropertiesCustomRepository<DataStorageConnectorMetaModelEntity> {
}
