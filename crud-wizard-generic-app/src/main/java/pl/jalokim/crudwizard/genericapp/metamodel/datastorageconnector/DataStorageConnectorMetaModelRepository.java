package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface DataStorageConnectorMetaModelRepository extends BaseRepository<DataStorageConnectorMetaModelEntity> {
}
