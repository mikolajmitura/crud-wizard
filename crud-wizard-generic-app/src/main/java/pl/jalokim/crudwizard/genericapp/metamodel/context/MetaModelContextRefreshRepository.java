package pl.jalokim.crudwizard.genericapp.metamodel.context;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaModelContextRefreshRepository extends JpaRepository<MetaModelContextRefreshEntity, Long> {

}
