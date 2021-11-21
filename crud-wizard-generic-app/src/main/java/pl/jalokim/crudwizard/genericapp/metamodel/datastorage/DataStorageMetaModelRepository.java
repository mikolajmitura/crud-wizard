package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataStorageMetaModelRepository extends JpaRepository<DataStorageMetaModelEntity, Long> {

    boolean existsByNameAndClassName(String name, String className);

}
