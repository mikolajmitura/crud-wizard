package pl.jalokim.crudwizard.maintenance.endpoints;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndpointMetaModelRepository extends JpaRepository<EndpointMetaModelEntity, Long> {

}
