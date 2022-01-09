package pl.jalokim.crudwizard.genericapp.metamodel.configuration;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DefaultConfigurationRepository extends JpaRepository<DefaultConfigurationEntity, Long> {

    Optional<DefaultConfigurationEntity> findByPropertyNameAndValueRealClassName(String propertyName, String valueRealClassName);
}
