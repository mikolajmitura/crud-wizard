package pl.jalokim.crudwizard.genericapp.metamodel.apitag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiTagRepository extends JpaRepository<ApiTagEntity, Long> {

}
