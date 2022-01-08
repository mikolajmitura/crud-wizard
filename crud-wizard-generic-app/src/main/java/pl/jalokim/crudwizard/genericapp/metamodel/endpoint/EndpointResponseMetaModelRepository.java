package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointResponseMetaModelRepository extends JpaRepository<EndpointResponseMetaModelEntity, Long> {

}
