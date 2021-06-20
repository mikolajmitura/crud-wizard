package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface DataStorageMetaModelRepository extends JpaRepository<DataStorageMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<DataStorageMetaModelEntity> {

    boolean existsByNameAndClassName(String name, String className);
}
