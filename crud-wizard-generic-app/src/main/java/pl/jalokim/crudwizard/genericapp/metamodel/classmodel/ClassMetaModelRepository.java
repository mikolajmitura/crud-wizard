package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface ClassMetaModelRepository extends BaseRepository<ClassMetaModelEntity> {

    @Query("from ClassMetaModelEntity c "
        + "where c.className = :className and c.simpleRawClass = true")
    Optional<ClassMetaModelEntity> findByRawClassName(String className);
}
