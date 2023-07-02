package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface ClassMetaModelRepository extends BaseRepository<ClassMetaModelEntity> {

    List<ClassMetaModelEntity> findByClassName(String className);
}
