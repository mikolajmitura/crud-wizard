package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface ValidatorMetaModelRepository extends JpaRepository<ValidatorMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<ValidatorMetaModelEntity> {

    @Query("from ValidatorMetaModelEntity v "
        + "where v.parametrized = false "
        + "and v.className = :className ")
    Optional<ValidatorMetaModelEntity> findByClassName(String className);

    @Query("from ValidatorMetaModelEntity v "
        + "where v.parametrized = false "
        + "and v.validatorName = :validatorName")
    Optional<ValidatorMetaModelEntity> findByValidatorName(String validatorName);
}
