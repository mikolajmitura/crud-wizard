package pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalPropertyRepository extends JpaRepository<AdditionalPropertyEntity, Long> {

    List<AdditionalPropertyEntity> findByOwnerIdAndOwnerClass(Long ownerId, String ownerClass);
}

