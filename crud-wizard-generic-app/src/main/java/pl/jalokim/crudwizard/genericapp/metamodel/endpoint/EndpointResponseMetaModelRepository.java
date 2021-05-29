package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface EndpointResponseMetaModelRepository extends JpaRepository<EndpointResponseMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<EndpointResponseMetaModelEntity> {

}
