package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface ClassMetaModelRepository extends JpaRepository<ClassMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<ClassMetaModelEntity> {

}
