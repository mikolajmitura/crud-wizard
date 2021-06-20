package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdditionalPropertyRepository extends JpaRepository<AdditionalPropertyEntity, Long> {

    List<AdditionalPropertyEntity> findByOwnerIdAndOwnerClass(Long ownerId, String ownerClass);

    Optional<AdditionalPropertyEntity> findByNameAndValueRealClassName(String name, String valueRealClassName);
}

