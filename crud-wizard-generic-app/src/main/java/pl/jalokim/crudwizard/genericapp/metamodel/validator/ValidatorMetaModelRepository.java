package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface ValidatorMetaModelRepository extends JpaRepository<ValidatorMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<ValidatorMetaModelEntity> {

}
