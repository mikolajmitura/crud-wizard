package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface DataStorageMetaModelRepository extends BaseRepository<DataStorageMetaModelEntity> {

    boolean existsByNameAndClassName(String name, String className);

}
