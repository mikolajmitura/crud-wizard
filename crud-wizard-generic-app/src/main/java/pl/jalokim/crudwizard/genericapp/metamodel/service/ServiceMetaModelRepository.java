package pl.jalokim.crudwizard.genericapp.metamodel.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface ServiceMetaModelRepository extends JpaRepository<ServiceMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<ServiceMetaModelEntity> {

}
