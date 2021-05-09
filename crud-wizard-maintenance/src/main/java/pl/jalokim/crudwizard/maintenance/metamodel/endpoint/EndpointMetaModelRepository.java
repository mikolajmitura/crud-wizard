package pl.jalokim.crudwizard.maintenance.metamodel.endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface EndpointMetaModelRepository extends JpaRepository<EndpointMetaModelEntity, Long>, WithAdditionalPropertiesCustomRepository {

}
