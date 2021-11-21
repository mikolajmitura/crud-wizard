package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassMetaModelRepository extends JpaRepository<ClassMetaModelEntity, Long> {

    @Query("from ClassMetaModelEntity c "
        + "where c.className = :className and c.simpleRawClass = true")
    Optional<ClassMetaModelEntity> findByRawClassName(String className);
}
